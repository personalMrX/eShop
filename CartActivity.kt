package com.example.nerus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nerus.Model.Cart
import com.example.nerus.Prevalent.Prevalent
import com.example.nerus.ViewHolder.CartViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_cart.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NAME_SHADOWING")
class CartActivity : AppCompatActivity() {
	private lateinit var recyclerView: RecyclerView
	private var overTotalPrice: Int = 0

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_cart)
		recyclerView = findViewById(R.id.cart_list)
		recyclerView.setHasFixedSize(true)
		val linearLayoutManager = LinearLayoutManager(this)
		recyclerView.layoutManager = linearLayoutManager

		total_price.text = "Стоимость = $overTotalPrice руб"
		next_btn.setOnClickListener {
			total_price.text = "Стоимость = $overTotalPrice руб"
			val intent = Intent(this@CartActivity, ConfirmFinalOrderActivity::class.java)
			intent.putExtra("Total Price", overTotalPrice.toString())
			startActivity(intent)
			finish()
		}

		back_home.setOnClickListener {
			startActivity(Intent(this@CartActivity,HomeActivity::class.java))
		}
	}


	override fun onStart() {
		super.onStart()
		OrderState()
		val cartListRef = FirebaseDatabase.getInstance().reference.child("Cart List")

		val options = FirebaseRecyclerOptions.Builder<Cart>()
			.setQuery(cartListRef.child("User View").child(Prevalent.currentOnlineUser.phone).child("Products"), Cart::class.java
			).build()

		val adapter = object : FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
			override fun onCreateViewHolder(parent: ViewGroup, p1: Int): CartViewHolder {
				val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_items_layout, parent, false)
				return CartViewHolder(view)
			}

			@SuppressLint("SetTextI18n")
			override fun onBindViewHolder(holder: CartViewHolder, position: Int, model: Cart) {
				holder.txtProductQuantity.text = "Количество = ${model.quantity}"
				holder.txtProductPrice.text = "Цена ${model.price} руб."
				holder.txtProductName.text = model.pname

				val pr = Integer.valueOf(model.price)

				val qua = Integer.valueOf(model.quantity)

				val oneTypeProductTPrice = pr * qua
				overTotalPrice += oneTypeProductTPrice

				holder.itemView.setOnClickListener {
					val options = arrayOf<CharSequence>("Редактировать", "Удалить")
					val builder = AlertDialog.Builder(this@CartActivity)
					builder.setTitle("\n" + "Настройки корзины:")
					builder.setItems(options) { dialogInterface, i ->
						if (i == 0) {
							val intent = Intent(this@CartActivity, ProductDetailsActivity::class.java)
							intent.putExtra("pid", model.pid)
							startActivity(intent)
						}
						if (i == 1) {
							cartListRef.child("User View").child(Prevalent.currentOnlineUser.phone)
								.child("Products").child(model.pid).removeValue()
								.addOnCompleteListener { task ->
									if (task.isSuccessful) {
										startActivity(Intent(this@CartActivity, HomeActivity::class.java))
									}
								}
						}
					}
					builder.show()
				}
			}
		}
		recyclerView.adapter = adapter
		adapter.startListening()
}


private fun OrderState() {
	val ordersRef: DatabaseReference = FirebaseDatabase.getInstance()
		.reference.child("Orders").child(Prevalent.currentOnlineUser.phone)

	ordersRef.addValueEventListener(object : ValueEventListener {
		@SuppressLint("SetTextI18n")
		override fun onDataChange(dataSnapshot: DataSnapshot) {
			if (dataSnapshot.exists()) {
				val shippingState = dataSnapshot.child("state").value!!.toString()
				val userName = dataSnapshot.child("name").value!!.toString()

				if (shippingState == "отправлено") {
					total_price.text = "Уважаемый $userName\n ваш заказ принят."
					recyclerView.visibility = View.GONE

					msg1.visibility = View.VISIBLE
					msg1.text = "Ваш заказ принят, ожидайте доставки."
					next_btn.visibility = View.GONE

					Toast.makeText(
						this@CartActivity, "\n" +
								"Вы можете приобрести больше продуктов, как только вы получили свой первый окончательный заказ.",
						Toast.LENGTH_SHORT
					).show()
				} else if (shippingState == "не отправлено") {
					total_price.text = "Состояние доставки = Не отправлено"
					recyclerView.visibility = View.GONE

					msg1.visibility = View.VISIBLE
					next_btn.visibility = View.GONE
				}
			}
		}

		override fun onCancelled(databaseError: DatabaseError) {
		}
	})
}
}



