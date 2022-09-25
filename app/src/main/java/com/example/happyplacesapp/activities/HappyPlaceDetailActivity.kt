package com.example.happyplacesapp.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplacesapp.MainActivity
import com.example.happyplacesapp.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplacesapp.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding: ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        supportActionBar?.hide() //this is for hide action bar
        binding!!.btnBack.setOnClickListener {
            onBackPressed()
        }

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // get the Serializable data model class with the details in it
            happyPlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null){
            supportActionBar!!.title = happyPlaceDetailModel.title

            //set data from received into activity
            binding!!.ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding!!.tvDescription.text = happyPlaceDetailModel.description
            binding!!.tvLocation.text = happyPlaceDetailModel.location
        }

    }
}