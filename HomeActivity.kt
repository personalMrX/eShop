package com.example.nerus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.TextView
import com.example.nerus.Admin.AdminAddNewProductActivity
import com.example.nerus.Admin.AdminCategoryActivity
import com.example.nerus.Admin.AdminMaintainProductsActivity
import com.example.nerus.Auth.LogInActivity
import com.example.nerus.Model.Products
import com.example.nerus.Prevalent.Prevalent
import com.example.nerus.ViewHolder.ProductViewHolder
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_admin_category.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.app_bar_home.*
import java.security.AccessController.getContext as getContext1


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
	private lateinit var recyclerView: RecyclerView
	private var type: String = "User"


	@SuppressLint("RestrictedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_home)

		val intent = intent
		val bundle = intent.extras
		if (bundle != null)
			type = getIntent().extras!!.get("Admin").toString()
		Paper.init(this)
		val linearLayoutManager = LinearLayoutManager(this)
		val toolbar: Toolbar = findViewById(R.id.toolbar)
		toolbar.title = "Товары"
		setSupportActionBar(toolbar)

		if (type != "Admin") {
			fab.visibility = View.VISIBLE
			val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
			val toggle = ActionBarDrawerToggle(
				this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
			)
			drawerLayout.addDrawerListener(toggle)
			toggle.syncState()
			fab.setOnClickListener {
				startActivity(Intent(this@HomeActivity, CartActivity::class.java))
			}
		} else {
			fab.visibility = View.INVISIBLE
			toolbar.setNavigationIcon(R.drawable.ic_back)
			add_new.visibility = View.VISIBLE
			toolbar.setOnClickListener {
				startActivity(Intent(this@HomeActivity, AdminCategoryActivity::class.java))
			}
			add_new.setOnClickListener {
				startActivity(Intent(this@HomeActivity, AdminAddNewProductActivity::class.java))
			}
		}

		val navView: NavigationView = findViewById(R.id.nav_view)
		navView.setNavigationItemSelectedListener(this)
		val userNameTextView = nav_view.getHeaderView(0).findViewById<TextView>(R.id.user_profile_name)
		val profileImageView = nav_view.getHeaderView(0).findViewById<CircleImageView>(R.id.user_profile_image)
		if (type != "Admin") {
//			userNameTextView.text = Prevalent.currentOnlineUser.name
			//if (Prevalent.currentOnlineUser.image != null)
				//Picasso.get().load(Prevalent.currentOnlineUser.image).into(profileImageView)
		}

		recyclerView = findViewById(R.id.recycler_menu)
		recyclerView.setHasFixedSize(true)
		recyclerView.layoutManager = linearLayoutManager as RecyclerView.LayoutManager?
	}

	override fun onStart() {
		super.onStart()
		val productsRef: DatabaseReference = FirebaseDatabase.getInstance()
			.reference.child("Products")
		val options = FirebaseRecyclerOptions.Builder<Products>()
			.setQuery(productsRef, Products::class.java)
			.build()
		val adapter = object : FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {
			@SuppressLint("SetTextI18n")
			override fun onBindViewHolder(holder: ProductViewHolder, position: Int, model: Products) {
				holder.txtProductName.text = model.pname
				holder.txtProductDescription.text = model.description
				holder.txtProductPrice.text = "Price: ${model.price} руб"
				holder.imageView.setOnClickListener {
					if (type == "Admin") {
						val intent = Intent(this@HomeActivity,
							AdminMaintainProductsActivity::class.java)
						intent.putExtra("pid", model.pid)
						startActivity(intent)
					} else {
						val intent = Intent(this@HomeActivity,
							ProductDetailsActivity::class.java)
						intent.putExtra("pid", model.pid)
						startActivity(intent)
					}
				}
				Picasso.get().load(model.image).into(holder.imageView)
			}

			override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ProductViewHolder {
				return ProductViewHolder(
					LayoutInflater.from(parent.context)
						.inflate(R.layout.product_items_layout, parent, false)
				)
			}
		}
		recyclerView.adapter = adapter
		adapter.startListening()
	}


	override fun onBackPressed() {
		val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START)
		} else {
			super.onBackPressed()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.home, menu)
		//R.id.app_bar_search
		return true
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		if (id == R.id.nav_cart) {
			if (type != "Admin") {
				val intent = Intent(this@HomeActivity, CartActivity::class.java)
				startActivity(intent)
			}
		} else if (id == R.id.nav_search) {
			if (type != "Admin") {
				val intent = Intent(this@HomeActivity, SearchProductsActivity::class.java)
				startActivity(intent)
			}
		} else if (id == R.id.nav_settings) {
			if (type != "Admin") {
				val intent = Intent(this@HomeActivity, SettinsActivity::class.java)
				startActivity(intent)
			}
		} else if (id == R.id.nav_logout) {
			if (type != "Admin") {
				Paper.book().destroy()
				val intent = Intent(this@HomeActivity, LogInActivity::class.java)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
				startActivity(intent)
				finish()
			}
		}

		val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
		drawer.closeDrawer(GravityCompat.START)
		return true
	}
}


