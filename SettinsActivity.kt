@file:Suppress("DEPRECATION")

package com.example.nerus

import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_settins.*
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.example.nerus.Prevalent.Prevalent
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SettinsActivity : AppCompatActivity() {
	companion object {
		val TAG = "SettinsActivity"
	}

	private var selectedPhotoUri: Uri? = null
	private lateinit var downloadImageUrl: String

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settins)

		userInfoDisplay()

		close_settings_btn.setOnClickListener {
			finish()
		}

		update_account_settings_btn.setOnClickListener {
			userInfoSaved()
		}

		profile_image_change_btn.setOnClickListener {
			Log.d(TAG, "Try to show photo selector")
			val intent = Intent(Intent.ACTION_PICK)
			intent.type = "image/*"
			startActivityForResult(intent, 0)
		}

		settings_profile_image.setOnClickListener {
			Log.d(TAG, "Try to show photo selector")
			val intent = Intent(Intent.ACTION_PICK)
			intent.type = "image/*"
			startActivityForResult(intent, 0)
		}

		/*security_questions_btn.setOnClickListener {
			val intent = Intent(this@SettinsActivity, ResetPasswordActivity::class.java)
			intent.putExtra("check", "settings")
			startActivity(intent)
		}*/
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
			selectedPhotoUri = data.data
			val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
			settings_profile_image.setImageBitmap(bitmap)
		}
	}

	private fun userInfoSaved() {
		if (settings_full_name.text.toString().isEmpty())
			Toast.makeText(this, "Имя обязательно.", Toast.LENGTH_SHORT).show()
		else if (settings_address.text.toString().isEmpty())
			Toast.makeText(this, "Заполните адресс.", Toast.LENGTH_SHORT).show()
		else if (settings_phone_number.text.toString().isEmpty())
			Toast.makeText(this, "Заполните номер.", Toast.LENGTH_SHORT).show()
		else if (selectedPhotoUri == null)
			Toast.makeText(this, "Выберите фотографию.", Toast.LENGTH_SHORT).show()
		else
			uploadImageToFirebaseStorage()
	}


	private fun uploadImageToFirebaseStorage() {
		val progressDialog: ProgressDialog = ProgressDialog(this)
		progressDialog.setTitle("Обновление аккаунта")
		progressDialog.setMessage("Пожалуйста, подождите, пока мы обновляем данные вашей учетной записи")
		progressDialog.setCanceledOnTouchOutside(false)
		progressDialog.show()
		val filename = UUID.randomUUID().toString()
		val ref = FirebaseStorage.getInstance().reference.child("Profile pictures")
		val filePath = ref.child("$filename.jpg")
		val uploadTask = filePath.putFile(this!!.selectedPhotoUri!!)
		uploadTask.addOnFailureListener { e ->
			val message = e.toString()
			Toast.makeText(this@SettinsActivity, "Ошибка: $message", Toast.LENGTH_SHORT).show()
		}
			.addOnSuccessListener {
				Toast.makeText(this@SettinsActivity, "Фотография товара успешна загружена...", Toast.LENGTH_SHORT)
					.show()
				uploadTask.continueWithTask { task ->
					if (!task.isSuccessful) {
						throw task.exception!!
					}
					downloadImageUrl = filePath.downloadUrl.toString()
					filePath.downloadUrl
				}
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							downloadImageUrl = task.result.toString()
							Toast.makeText(this@SettinsActivity, "получен Uri изображения..", Toast.LENGTH_SHORT).show()
							updateAccountInfo()
						}
					}
			}
	}

	private fun updateAccountInfo() {
		val ref = FirebaseDatabase.getInstance().reference.child("Users")
		val userMap = HashMap<String, String>()
		userMap["name"] = settings_full_name.text.toString()
		userMap["address"] = settings_address.text.toString()
		userMap["phoneOrder"] = settings_phone_number.text.toString()
		userMap["image"] = downloadImageUrl
		ref.child(Prevalent.currentOnlineUser.phone).updateChildren(userMap as Map<String, Any>)
		startActivity(Intent(this@SettinsActivity, HomeActivity::class.java))
		Toast.makeText(this@SettinsActivity,
			"Данные успешно обновлены.", Toast.LENGTH_SHORT).show()
		finish()
	}


	private fun userInfoDisplay() {
		val usersRef: DatabaseReference = FirebaseDatabase.getInstance()
			.reference.child("Users").child(Prevalent.currentOnlineUser.phone)
		usersRef.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				if (dataSnapshot.exists()) {
					//val image = dataSnapshot.child("image").value.toString()
					val name = dataSnapshot.child("name").value.toString()
					val phone = dataSnapshot.child("phone").value.toString()
					val address = dataSnapshot.child("address").value.toString()
					settings_full_name.setText(name)
					settings_phone_number.setText(phone)
					if (Prevalent.currentOnlineUser.address != null)
						settings_address.setText(address)
					else
						settings_address.setText("")
					if (Prevalent.currentOnlineUser.image != null) {
						Picasso.get().load(Prevalent.currentOnlineUser.image).into(settings_profile_image)
					}
				}
			}

			override fun onCancelled(databaseError: DatabaseError?) {
			}
		})
	}
}
