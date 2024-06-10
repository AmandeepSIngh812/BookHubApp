package com.amandeep.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amandeep.bookhub.R
import com.amandeep.bookhub.adapter.DashboardRecyclerAdapter
import com.amandeep.bookhub.model.Book
import com.amandeep.bookhub.util.ConnectionManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.Collections

class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var recyclerAdapter: DashboardRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    val bookInfoList: ArrayList<Book> = ArrayList<Book>()
    lateinit var progressLayout: RelativeLayout
    lateinit var progessBar: ProgressBar

    var ratingComparator = Comparator<Book>{book1, book2 ->
       if( book1.bookRating. compareTo(book2.bookRating, true)== 0){

           book1.bookName.compareTo(book2.bookName, true)
       }else{
         book1.bookRating.compareTo(book2.bookRating,true)
       }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)
        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        progressLayout = view.findViewById(R.id.progressLayout)
        progessBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE

        layoutManager = LinearLayoutManager(activity)


        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = object :
                JsonObjectRequest(Method.GET, url, null, Response.Listener {

                    try {
                        //Here we will handle the response
                        progressLayout.visibility = View.GONE
                        val success = it.getBoolean("success")
                        if (success) {
                            val data = it.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )
                                bookInfoList.add(bookObject)
                                recyclerAdapter =
                                    DashboardRecyclerAdapter(activity as Context, bookInfoList)
                                recyclerDashboard.adapter = recyclerAdapter
                                recyclerDashboard.layoutManager = layoutManager
                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some Error has Occurred",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected Error has Occurred!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }, Response.ErrorListener {
                    //Here we will handle the errors
                    if (activity != null) {
                        Toast.makeText(activity as Context, "Volley as error", Toast.LENGTH_LONG)
                            .show()
                    }

                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9c55549d6d7525"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Failed Connection")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Settings") { text, listenner ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { text, listenner ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList,ratingComparator)
            bookInfoList.reverse()
        }
        recyclerAdapter.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }
}