package com.techbros.myproject.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.techbros.myproject.UIState
import com.techbros.myproject.model.Test

import com.google.firebase.database.*

class HomeViewModel : ViewModel() {
    private val _tests = MutableLiveData<List<Test>>()
    val tests: LiveData<List<Test>> get() = _tests

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("results")

    init {
        loadLastFiveTestsFromFirebase()
    }

    private fun loadLastFiveTestsFromFirebase() {
        /*val query = database.orderByKey().limitToLast(5)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val testList = mutableListOf<Test>()
                for (dataSnapshot in snapshot.children) {
                    val test = dataSnapshot.getValue(Test::class.java)
                    test?.let { testList.add(it) }
                }
                _tests.value = testList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })*/
    }
}

