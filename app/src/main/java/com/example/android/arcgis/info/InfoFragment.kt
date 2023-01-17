package com.example.android.arcgis.info

import android.content.Intent
import android.graphics.Color.blue
import android.net.Uri
import android.net.Uri.parse
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.android.arcgis.R
import com.example.android.arcgis.databinding.FragmentInfoBinding
import com.example.android.arcgis.map.MapFragment
import java.net.URI

class InfoFragment : Fragment() {

    companion object {
        private val TAG: String = InfoFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentInfoBinding

    private val args: InfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentInfoBinding.inflate(inflater)

        binding.infoName.text = args.name
        binding.infoState.text = getString(R.string.location, args.state)
        binding.infoDescription.text = getString(R.string.about, args.description)
        setUpHyperlink(binding.infoUrl)
        setUpImage(binding.infoImage)

        return binding.root
    }

    private fun setUpImage(infoImage: ImageView) {
        Glide.with(this)
            .load(parse(args.image))
            .into(infoImage)
    }

    private fun setUpHyperlink(infoUrl: Button) {
        infoUrl.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, parse(args.url))
            startActivity(browserIntent)
        }

    }

}