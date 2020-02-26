package com.example.nerus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.nerus.Prevalent.Prevalent
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_confirm_final_order.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


@Suppress("NAME_SHADOWING")
class ConfirmFinalOrderActivity : AppCompatActivity() {
	private var totalAmount = ""


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_confirm_final_order)

		userInfoDisplay()
		totalAmount = intent.getStringExtra("Total Price")
		Toast.makeText(this, "Итоговая цена = $totalAmount руб.", Toast.LENGTH_SHORT).show()
		confirm_final_order_btn.setOnClickListener {
			Check()
		}
		confirm_final_order_cancel.setOnClickListener {
			startActivity(Intent(this@ConfirmFinalOrderActivity,CartActivity::class.java))
		}
	}

	private fun userInfoDisplay() {
		val usersRef: DatabaseReference = FirebaseDatabase.getInstance()
			.reference.child("Users").child(Prevalent.currentOnlineUser.phone)
		usersRef.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				if (dataSnapshot.exists()) {
					val name = dataSnapshot.child("name").value.toString()
					val phone = dataSnapshot.child("phone").value.toString()
					val address = dataSnapshot.child("address").value.toString()
					shippment_name.setText(name)
					shippment_phone_number.setText(phone)
					if (Prevalent.currentOnlineUser.address !=null)
						shippment_address.setText(address)
					else
						shippment_address.setText("")
				}
			}
			override fun onCancelled(databaseError: DatabaseError?) {
			}
		})
	}

	private fun Check() {
		val nameEditText = shippment_name.text.toString()
		val phoneEditText = shippment_phone_number.text.toString()
		val addressEditText = shippment_address.text.toString()
		val cityEditText = shippment_city.text.toString()
		if (nameEditText.isEmpty()) {
			Toast.makeText(this, "Пожалуйста, укажите свое полное имя.", Toast.LENGTH_SHORT).show()
		} else if (phoneEditText.isEmpty()) {
			Toast.makeText(this, "Пожалуйста, укажите свой номер телефона.", Toast.LENGTH_SHORT).show()
		} else if (addressEditText.isEmpty()) {
			Toast.makeText(this, "Пожалуйста, укажите ваш адрес.", Toast.LENGTH_SHORT).show()
		} else if (cityEditText.isEmpty()) {
			Toast.makeText(this, "Пожалуйста, укажите название вашего города.", Toast.LENGTH_SHORT).show()
		} else {
			ConfirmOrder(nameEditText, phoneEditText, addressEditText, cityEditText)
		}
	}

	@SuppressLint("SimpleDateFormat")
	private fun ConfirmOrder(name: String, phone: String, address: String, city: String) {
		val saveCurrentDate: String
		val saveCurrentTime: String
		val calForDate = Calendar.getInstance()
		val currentDate = SimpleDateFormat("MMM dd, yyyy")
		saveCurrentDate = currentDate.format(calForDate.time)

		val currentTime = SimpleDateFormat("HH:mm:ss ")
		saveCurrentTime = currentTime.format(calForDate.time)

		val ordersRef = FirebaseDatabase.getInstance().reference
			.child("Orders")
			.child(Prevalent.currentOnlineUser.phone)

		val ordersMap = HashMap<String, String>()
		ordersMap["totalAmount"] = totalAmount
		ordersMap["name"] = name
		ordersMap["phone"] = phone
		ordersMap["address"] = address
		ordersMap["city"] = city
		ordersMap["date"] = saveCurrentDate
		ordersMap["time"] = saveCurrentTime
		ordersMap["state"] = "не отправлено"
		ordersRef.updateChildren(ordersMap as Map<String, Any>?).addOnCompleteListener { task ->
			if (task.isSuccessful) {
				FirebaseDatabase.getInstance().reference
					.child("Cart List")
					.child("User View")
					.child(Prevalent.currentOnlineUser.phone)
					.removeValue()
					.addOnCompleteListener { task ->
						if (task.isSuccessful) {
							Toast.makeText(
								this@ConfirmFinalOrderActivity,
								"Ваш последний заказ был успешно размещен.",
								Toast.LENGTH_SHORT
							).show()
							val intent = Intent(this@ConfirmFinalOrderActivity, HomeActivity::class.java)
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
							startActivity(intent)
							finish()
						}
					}
			}
		}
	}
}