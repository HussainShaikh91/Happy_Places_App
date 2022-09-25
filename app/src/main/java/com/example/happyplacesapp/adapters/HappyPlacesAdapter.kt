package com.example.happyplacesapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.MainActivity
import com.example.happyplacesapp.activities.AddHappyPlaceActivity
import com.example.happyplacesapp.database.DatabaseHandler
import com.example.happyplacesapp.databinding.ItemHappyPlaceBinding
import com.example.happyplacesapp.models.HappyPlaceModel

class HappyPlacesAdapter(private val context: Context,
                         private var list: ArrayList<HappyPlaceModel>):
    RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

    //this is for click listener
    var onItemClick : ((HappyPlaceModel) -> Unit)? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: HappyPlacesAdapter.ViewHolder, position: Int) {
        val model = list[position]

        //set date into view
        holder.ivImage.setImageURI(Uri.parse(model.image))
        holder.tvTitles.text = model.title
        holder.tvDescription.text = model.description


        /*
        * function to bind item to clickListener
        * */
        holder.itemView.setOnClickListener {
            //call
            onItemClick?.invoke(model)
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A function to edit the added happy place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity,position: Int,requestCode: Int){
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])

        activity.startActivityForResult(intent,requestCode) // Activity is started with requestCode

        notifyItemChanged(position)  // Notify any registered observers that the item at position has changed.
    }

    // TODO (Step 4: Create a function to delete the happy place details which is inserted earlier from the local storage.)
    // START
    /**
     * A function to delete the added happy place detail from the local storage.
     */
    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])

        if (isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position) //Notify adapter
        }
    }
    // END

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class ViewHolder (binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root){
        // Holds the TextView that will add each item to
        val tvTitles = binding.tvTitle
        val tvDescription = binding.tvDescription
        val ivImage = binding.ivPlaceImage

    }



}