package kr.devdogs.kotlinbook.phonebook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import android.widget.ListView

import java.util.ArrayList

import io.realm.Realm
import kr.devdogs.kotlinbook.phonebook.activity.FormActivity
import kr.devdogs.kotlinbook.phonebook.adapter.PhoneBookListAdapter
import kr.devdogs.kotlinbook.phonebook.model.PhoneBook

class MainActivity : AppCompatActivity() {
    private var phoneBookListView: ListView? = null
    private var insertBtn: Button? = null
    private var searchText: EditText? = null
    private var items: ArrayList<PhoneBook>? = null
    private var adapter: PhoneBookListAdapter? = null
    private var realm: Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionCheck()
        initView()

        Realm.init(applicationContext)
        realm = Realm.getDefaultInstance()
        items = ArrayList<PhoneBook>()
        adapter = PhoneBookListAdapter(this, R.layout.phonebook_listitem, items)
        phoneBookListView?.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        findByName(searchText?.text.toString())
    }

    private fun initView() {
        phoneBookListView = findViewById(R.id.main_tel_list) as ListView
        insertBtn = findViewById(R.id.main_btn_insert) as Button
        searchText = findViewById(R.id.main_search_text) as EditText

        insertBtn?.setOnClickListener {
            val insertViewIntent = Intent(this@MainActivity, FormActivity::class.java)
            startActivity(insertViewIntent)
        }

        searchText?.setOnKeyListener { _, _, _ ->
            findByName(searchText?.text.toString())
            false
        }
    }

    private fun findByName(name: String) {
        items?.clear()
        val allUser = realm!!.where<PhoneBook>(PhoneBook::class.java)
                .beginsWith("name", name)
                .findAll()
                .sort("name")
        for (p in allUser) {
            items?.add(p)
        }

        adapter?.notifyDataSetChanged()
    }

    private fun permissionCheck() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            var permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            }

            permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }

            permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }
}
