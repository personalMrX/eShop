package com.example.nerus

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.nerus.Model.Products
import com.example.nerus.Prevalent.Prevalent
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_product_details.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ProductDetailsActivity : AppCompatActivity() {
	private var productID = ""
	private var state = "Normal"


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product_details)
		productID = intent.getStringExtra("pid")

		getProductDetails(productID)

		pd_add_to_cart_button.setOnClickListener {
			if (state == "Заказ размещен" || state == "Заказ отправлен") {
				Toast.makeText(
					this@ProductDetailsActivity,
					"Вы можете добавить покупку больше продуктов, как только ваш заказ будет отправлен или подтвержден",
					Toast.LENGTH_LONG
				).show()
			} else {
				addingToCartList()
			}
		}
		pd_cancel_to_cart_button.setOnClickListener {
			startActivity(Intent (this@ProductDetailsActivity, HomeActivity::class.java))
		}
	}



	override fun onStart() {
		super.onStart()

		CheckOrderState()
	}

	private fun addingToCartList() {
		val saveCurrentTime: String
		val saveCurrentDate: String

		val calForDate = Calendar.getInstance()
		val currentDate = SimpleDateFormat("MMM dd, yyyy")
		saveCurrentDate = currentDate.format(calForDate.time)

		val currentTime = SimpleDateFormat("HH:mm:ss ")
		saveCurrentTime = currentDate.format(calForDate.time)

		val cartListRef = FirebaseDatabase.getInstance().reference.child("Cart List")

		val cartMap = HashMap<String,String>()
		cartMap["pid"] = productID
		cartMap["pname"] = product_name_details.text.toString()
		cartMap["price"] = product_price_details.text.toString()
		cartMap["date"] = saveCurrentDate
		cartMap["time"] = saveCurrentTime
		cartMap["quantity"] = number_btn.number
		cartMap["discount"] = ""

		cartListRef.child("User View").child(Prevalent.currentOnlineUser.phone).child("Products").child(productID)
			.updateChildren(cartMap as Map<String, Any>?)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.phone)
						.child("Products").child(productID)
						.updateChildren(cartMap as Map<String, Any>?)
						.addOnCompleteListener { task ->
							if (task.isSuccessful) {
								Toast.makeText(this@ProductDetailsActivity, "\n" +
										"Добавлено в корзину.", Toast.LENGTH_SHORT).show()
								startActivity(Intent(this@ProductDetailsActivity, HomeActivity::class.java))
							}
						}
				}
			}
	}


	private fun getProductDetails(productID: String) {
		val productsRef = FirebaseDatabase.getInstance().reference.child("Products")

		productsRef.child(productID).addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				if (dataSnapshot.exists()) {
					val products = dataSnapshot.getValue(Products::class.java)

					product_name_details.text = products!!.pname
					product_price_details.text = products.price
					product_description_details.text = products.description
					Picasso.get().load(products.image).into(product_image_details)
				}
			}

			override fun onCancelled(databaseError: DatabaseError) {

			}
		})
	}

	private fun CheckOrderState() {
		val ordersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Orders").child(Prevalent.currentOnlineUser.phone)

		ordersRef.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				if (dataSnapshot.exists()) {
					val shippingState = dataSnapshot.child("state").value!!.toString()

					if (shippingState == "отпралено") {
						state = "Заказ отправлен"
					} else if (shippingState == "не отправлено") {
						state = "Заказ размещен"
					}
				}
			}

			override fun onCancelled(databaseError: DatabaseError) {

			}
		})
	}
}
