// PhoneNumberFragment.kt
package com.jang.tel_project

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class PhoneNumberFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var phoneNumberAdapter: PhoneNumberAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone_number, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        phoneNumberAdapter = PhoneNumberAdapter()
        recyclerView.adapter = phoneNumberAdapter
        return view
    }

    fun setData(phoneNumberData: JSONArray) {
        phoneNumberAdapter.setData(phoneNumberData)
    }
}