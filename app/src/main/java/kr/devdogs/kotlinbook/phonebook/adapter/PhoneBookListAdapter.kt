package kr.devdogs.kotlinbook.phonebook.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


import kr.devdogs.kotlinbook.phonebook.R
import kr.devdogs.kotlinbook.phonebook.activity.FormActivity
import kr.devdogs.kotlinbook.phonebook.model.PhoneBook

/**
 * Created by Daniel on 2017. 9. 24..
 */

class PhoneBookListAdapter(context: Context,
                           private val viewId: Int,
                           var items: List<PhoneBook>?) : ArrayAdapter<PhoneBook>(context, viewId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        view ?: let {
            val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                    as LayoutInflater
            view = vi.inflate(this.viewId, null)
        }

        items?.let {
            val item = it[position]

            if (item != null) {
                val photoView = view!!.findViewById(R.id.book_item_photo) as ImageView
                val itemLayout = view!!.findViewById(R.id.book_item) as LinearLayout
                val nameView = view!!.findViewById(R.id.book_item_name) as TextView
                val callView = view!!.findViewById(R.id.book_item_call) as Button

                if (item.photoSrc == null) {
                    photoView.setImageDrawable(
                            context.getDrawable(R.drawable.icon_man))
                } else {
                    photoView.setImageBitmap(
                            BitmapFactory.decodeFile(Uri.parse(item.photoSrc).path))
                }

                nameView.text = item.name
                callView.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + item.phone))
                    context.startActivity(intent)
                }

                itemLayout.setOnClickListener {
                    val modifyViewIntent = Intent(context, FormActivity::class.java)
                    modifyViewIntent.putExtra("mode", FormActivity.MODE_UPDATE)
                    modifyViewIntent.putExtra("bookId", item.id)
                    context.startActivity(modifyViewIntent)
                }
            }
        }

        return view
    }

}
