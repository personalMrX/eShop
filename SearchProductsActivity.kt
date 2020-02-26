package com.example.nerus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nerus.Model.Products
import com.example.nerus.ViewHolder.ProductViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_search_products.*


class SearchProductsActivity : AppCompatActivity() {
	lateinit var searchList: RecyclerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_search_products)
		searchList = findViewById(R.id.search_list)
		searchList.layoutManager = LinearLayoutManager(this@SearchProductsActivity)

		search_btn.setOnClickListener {
			onStart()
		}
		back_btn.setOnClickListener {
			finish()
		}
		search_product_name.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
				onStart()
				return@OnKeyListener true
			}
			false
		})
	}

	override fun onStart() {
		super.onStart()
		val SearchInput = search_product_name.text.toString()
		val reference = FirebaseDatabase.getInstance().reference.child("Products")
		val options = FirebaseRecyclerOptions.Builder<Products>()
			.setQuery(reference.orderByChild("pname").startAt(SearchInput).endAt(SearchInput+"\uf8ff"),Products::class.java).build()
		val adapter = object : FirebaseRecyclerAdapter<Products,ProductViewHolder>(options){
			@SuppressLint("SetTextI18n")
			override fun onBindViewHolder(holder: ProductViewHolder, position: Int, model: Products) {
				holder.txtProductName.text = model.pname
				holder.txtProductDescription.text = model.description
				holder.txtProductPrice.text = "Price = " + model.price + " руб."
				Picasso.get().load(model.image).into(holder.imageView)
				holder.itemView.setOnClickListener {
					val intent = Intent(this@SearchProductsActivity, ProductDetailsActivity::class.java)
					intent.putExtra("pid", model.pid)
					startActivity(intent)
				}
			}

			override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
				val view = LayoutInflater.from(parent.context).inflate(R.layout.product_items_layout, parent, false)
				return ProductViewHolder(view)
			}
		}

		searchList.adapter = adapter
		adapter.startListening()
	}
}