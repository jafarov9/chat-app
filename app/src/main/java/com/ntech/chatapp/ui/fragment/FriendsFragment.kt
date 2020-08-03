package com.ntech.chatapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ntech.chatapp.R
import com.ntech.chatapp.model.User
import com.ntech.chatapp.ui.activity.ChatActivity
import com.ntech.chatapp.ui.adapter.FriendsAdapter
import com.ntech.chatapp.util.Constants
import kotlinx.android.synthetic.main.fragment_friends.*

class FriendsFragment : Fragment(), FriendsAdapter.OnFriendsClickListener {

    private val mUserDatabase: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child(Constants.CHILD_USERS)
    }

    private lateinit var adapter: FriendsAdapter
    private var userList: ArrayList<User> = arrayListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        adapter = FriendsAdapter(activity!!, userList)
        adapter.setOnFriendClickListener(this)
        rvFriends.layoutManager = LinearLayoutManager(activity)
        rvFriends.adapter = adapter

        mUserDatabase.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                if (snapshot.value != null) {
                    try {

                        val model = snapshot.getValue(User::class.java)
                        val friendKey = snapshot.ref.key

                        if (!currentUserId.equals(friendKey)) {
                            userList.add(model!!)
                            adapter.notifyItemInserted(userList.size - 1)
                        }

                    } catch (e: Exception) {
                        Log.e("onChildAdded", e.message!!)
                    }
                }

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

        })

    }

    override fun onFriendClick(user: User) {

        mUserDatabase.orderByChild(Constants.CHILD_NAME).equalTo(user.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val clickedUserKey = snapshot.children.iterator().next().ref.key
                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra(Constants.EXTRA_NAME, user.name)
                    intent.putExtra(Constants.EXTRA_ID, clickedUserKey)
                    startActivity(intent)
                }


            })

    }
}