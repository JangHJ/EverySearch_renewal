// PhoneNumberFragment.kt
package com.jang.tel_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class PhoneNumberAdapter : RecyclerView.Adapter<PhoneNumberAdapter.ViewHolder>() {

    private val phoneNumberList: MutableList<Pair<String, String>> = mutableListOf()

    fun setData(phoneNumberData: JSONArray) {
        phoneNumberList.clear()
        for (i in 0 until phoneNumberData.length()) {
            val jsonObject: JSONObject = phoneNumberData.getJSONObject(i)
            val name: String = jsonObject.getString("name")
            val phone: String = jsonObject.getString("phone")
            phoneNumberList.add(Pair(name, phone))
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewName)
        val phoneTextView: TextView = itemView.findViewById(R.id.textViewPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_phone_number, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, phone) = phoneNumberList[position]
        holder.nameTextView.text = name
        holder.phoneTextView.text = phone
    }

    override fun getItemCount(): Int {
        return phoneNumberList.size
    }
}
