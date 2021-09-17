package co.za.bayandabuthelezi.assessment5

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnUploadData = findViewById<Button>(R.id.btnUploadData)

        btnUploadData.setOnClickListener {

            val person = getOldPerson()
            savePerson(person)
        }

        val btnRetrieveData = findViewById<Button>(R.id.btnRetrieveData)
        btnRetrieveData.setOnClickListener {
            retrievePerson()
        }

        val btnUpdatePerson = findViewById<Button>(R.id.btnUpdatePerson)
        btnUpdatePerson.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updateinfo(oldPerson, newPersonMap)
        }
    }

    private fun getOldPerson(): Person {
        val firstName = findViewById<EditText>(R.id.etFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.etLastName).text.toString()
        val age = findViewById<EditText>(R.id.etAge).text.toString().toInt()
        return Person(firstName , lastName , age)
    }

    private fun getNewPersonMap(): Map<String , Any> {
        val firstName = findViewById<EditText>(R.id.etNewFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.etNewLastName).text.toString()
        val age = findViewById<EditText>(R.id.etNewAge).text.toString()
        val map = mutableMapOf<String , Any>()
        if (firstName.isNotEmpty()){
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()){
            map["lastName"] = lastName
        }
        if (age.isNotEmpty()){
            map["age"] = age.toInt()
        }
        return map
    }
    private fun updateinfo(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO
    ).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if (personQuery.documents.isNotEmpty()){
            for (document in personQuery){
                try {
                    personCollectionRef.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }else{
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, "Person Does Not Exist", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrievePerson() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents){
                val person = document.toObject(Person::class.java)
                sb.append("$person\n")
            }

            val tvPersons = findViewById<TextView>(R.id.tvPersons)
            withContext(Dispatchers.Main){
                tvPersons.text = sb.toString()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully saved data.", Toast.LENGTH_LONG).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}


