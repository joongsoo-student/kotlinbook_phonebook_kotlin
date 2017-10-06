package kr.devdogs.kotlinbook.phonebook.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import java.io.FileNotFoundException

import io.realm.Realm
import kr.devdogs.kotlinbook.phonebook.R
import kr.devdogs.kotlinbook.phonebook.model.PhoneBook
import kr.devdogs.kotlinbook.phonebook.utils.BitmapUtils

class FormActivity : AppCompatActivity() {
    private var submitBtn: Button? = null
    private var photoView: ImageView? = null
    private var nameView: EditText? = null
    private var phoneView: EditText? = null
    private var emailView: EditText? = null

    private var realm: Realm? = null
    private var photoPath: String? = null

    private var actionView: LinearLayout? = null
    private var callBtn: Button? = null
    private var smsBtn: Button? = null
    private var deleteBtn: Button? = null

    private var mode: Int = MODE_INSERT
    private var currentPhoneBook: PhoneBook? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        Realm.init(applicationContext)
        realm = Realm.getDefaultInstance()

        initView()
        setMode()
    }


    private fun initView() {
        photoView = findViewById(R.id.form_photo) as ImageView
        nameView = findViewById(R.id.form_name) as EditText
        phoneView = findViewById(R.id.form_phone) as EditText
        emailView = findViewById(R.id.form_email) as EditText
        submitBtn = findViewById(R.id.form_submit) as Button
        actionView = findViewById(R.id.form_action_layout) as LinearLayout
        deleteBtn = findViewById(R.id.form_delete) as Button
        callBtn = findViewById(R.id.form_action_call) as Button
        smsBtn = findViewById(R.id.form_action_sms) as Button

        photoView?.setOnClickListener { getPhotoImage() }

        submitBtn?.setOnClickListener {
            val name = nameView?.text.toString()
            val phone = phoneView?.text.toString()
            val email = emailView?.text.toString()

            if ("" == name || "" == phone) {
                Toast.makeText(this@FormActivity,
                        "이름, 휴대폰은 필수입니다",
                        Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            realm?.executeTransaction { realm ->
                if (mode == MODE_INSERT) {
                    currentPhoneBook = PhoneBook()
                    val currentIdNum = realm
                            .where<PhoneBook>(PhoneBook::class.java)
                            .max("id")
                    val nextId = (currentIdNum?.toInt() ?: 0) + 1
                    currentPhoneBook?.id = nextId
                }

                currentPhoneBook?.let {
                    it.name = name
                    it.phone = phone
                    it.email = email
                    it.photoSrc = photoPath

                    realm.insertOrUpdate(it)
                }
            }

            finish()
        }
    }


    private fun setMode() {
        mode = intent.getIntExtra("mode", MODE_INSERT)

        if (mode == MODE_UPDATE) {
            val phoneId = intent.getIntExtra("bookId", -1)

            if (phoneId == -1) {
                Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT)
                finish()
                return
            }

            currentPhoneBook = realm!!.where<PhoneBook>(PhoneBook::class.java)
                    .equalTo("id", phoneId)
                    .findFirst()

            currentPhoneBook ?: let {
                Toast.makeText(this, "존재하지 않는 연락처입니다", Toast.LENGTH_SHORT)
                finish()
                return
            }

            nameView?.setText(currentPhoneBook!!.name)
            phoneView?.setText(currentPhoneBook!!.phone)
            emailView?.setText(currentPhoneBook!!.email)

            if (currentPhoneBook!!.photoSrc == null) {
                photoView?.setImageDrawable(
                        getDrawable(R.drawable.icon_man))
            } else {
                photoView?.setImageBitmap(
                        BitmapFactory.decodeFile(
                                Uri.parse(currentPhoneBook!!.photoSrc).path))
            }

            photoPath = currentPhoneBook!!.photoSrc
            actionView?.visibility = View.VISIBLE
            deleteBtn?.visibility = View.VISIBLE

            callBtn?.setOnClickListener {
                val uri = Uri.parse("tel:" + currentPhoneBook!!.phone)
                val intent = Intent(Intent.ACTION_DIAL, uri)
                startActivity(intent)
            }

            smsBtn?.setOnClickListener {
                val uri = Uri.parse("smsto:" + currentPhoneBook!!.phone)
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                startActivity(intent)
            }

            deleteBtn?.setOnClickListener {
                val ab = AlertDialog.Builder(this@FormActivity)

                ab.setTitle("정말 삭제하시겠습니까?")
                ab.setPositiveButton("예") { _, _ ->
                    realm?.executeTransaction {
                        currentPhoneBook!!.deleteFromRealm()
                    }
                    finish()
                }.setNegativeButton("아니오" ) { dialog, _ -> dialog.cancel() }
                ab.show()
            }
        }
    }


    private fun getPhotoImage() {
        val items = arrayOf("카메라에서 가져오기", "앨범에서 가져오기")
        val ab = AlertDialog.Builder(this)

        ab.setTitle("사진 가져오기")
        ab.setItems(items) { dialog, whichButton ->
            if (whichButton == SELECT_TAKE_PICTURE) {
                takePicture()
            } else if (whichButton == SELECT_PICK_GALARY) {
                getPhotoFromGalary()
            }
        }.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        ab.show()
    }


    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQ_TAKE_PICTURE)
    }


    private fun getPhotoFromGalary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_PICK_GALARY)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQ_TAKE_PICTURE) {
                val thumbnail = data.extras.get("data") as Bitmap
                var dst = Bitmap.createScaledBitmap(thumbnail, 100, 100, true)
                dst = BitmapUtils.rotate(dst, 90)

                photoPath = BitmapUtils.saveBitmap(dst)
                photoView?.setImageBitmap(dst)
            } else if (requestCode == REQ_PICK_GALARY) {
                try {
                    contentResolver.openInputStream(data.data).use {
                        var photo = BitmapFactory.decodeStream(it)
                        photo = Bitmap.createScaledBitmap(photo, 100, 100, true)

                        photoPath = BitmapUtils.saveBitmap(photo)
                        photoView?.setImageBitmap(photo)
                    }
                } catch (e: FileNotFoundException) {
                    Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                }
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }

    companion object {
        val MODE_INSERT = 100
        val MODE_UPDATE = 101

        private val REQ_TAKE_PICTURE = 200
        private val REQ_PICK_GALARY = 201

        private val SELECT_TAKE_PICTURE = 0
        private val SELECT_PICK_GALARY = 1
    }
}
