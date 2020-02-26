package com.example.nerus


class ResetPasswordActivity{
/*	private var check = ""
	private lateinit var phoneNumber: EditText


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_reset_password)

		val phoneNumber = find_phone_number.text.toString()
		check = intent.getStringExtra("check")
	}

	override fun onStart() {
		super.onStart()
		val pageTitle = page_title.text.toString()
		val titleQuestions = title_questions.text.toString()

		val questions1 = question_1.text.toString()
		val questions2 = question_2.text.toString()

		phoneNumber.visibility = View.GONE

		if (check == "settings") {
			page_title.text = "Set Questions"
			title_questions. text = "Пожалуйста придумайте секретный вопрос"
			verify_btn.text = "Задать"
			verify_btn.setOnClickListener {
				if (questions1 == "" && questions2== ""){
					Toast.makeText(this@ResetPasswordActivity,"Пожалуйтса ответьте на вопросы",Toast.LENGTH_SHORT).show()
				}
				else{
					val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
						.child(Prevalent.currentOnlineUser.phone)
					val userDataMap:HashMap<String,Any> = HashMap<String,Any>()
					userDataMap["answer1"] = questions1
					userDataMap["answer2"] = questions2
					ref.child("Security Questions").updateChildren(userDataMap)
						.addOnCompleteListener(object: OnCompleteListener<Void>){

						}
				}
			}

		}
		else if (check == "login") {
			ref = FirebaseDatabase.getInstance().reference.child("Users")
		}
	}*/
}
