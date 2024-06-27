package com.appmovil24.starproyect.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.appmovil24.starproyect.databinding.FragmentPreferencesBinding
import com.appmovil24.starproyect.repository.AuthenticationRepository
import java.util.Locale
import android.content.res.Configuration
import com.appmovil24.starproyect.R

class PreferencesFragment : Fragment() {

    private var _binding: FragmentPreferencesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutButton.setOnClickListener {
            AuthenticationRepository.signOut(requireActivity() as AppCompatActivity)
        }
        val currentLanguage = Locale.getDefault().language
        when (currentLanguage) {
            "en" -> binding.changeLanguageButton.check(R.id.select_english)
            "es" -> binding.changeLanguageButton.check(R.id.select_spanish)
            else -> binding.changeLanguageButton.check(R.id.select_english)
        }
        binding.changeLanguageButton.setOnCheckedChangeListener { group, checkedId ->
            val language = when (checkedId) {
                R.id.select_english -> "en"
                R.id.select_spanish -> "es"
                else -> "en"
            }
            changeLanguage(language)
        }
    }

    private fun changeLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
