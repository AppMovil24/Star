package com.appmovil24.starproyect.ui.userProfile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.appmovil24.starproyect.databinding.FragmentUserProfileBinding
//import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class UserProfileFragment : Fragment() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE_REQUEST = 2
    }

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAccountDTO: UserAccountDTO
    private lateinit var userAccountRepository: UserAccountRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        userAccountRepository = UserAccountRepository(auth.currentUser!!)

        userAccountRepository.get { accountDTO: UserAccountDTO? ->
            userAccountDTO = accountDTO !!
            displayUserInfo(userAccountDTO)
        }

        binding.cameraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

        binding.imageGaleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.userImage.setImageBitmap(imageBitmap)
            val imageUri = getImageUri(requireContext(), imageBitmap)
            saveImage(imageUri)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if(imageUri != null)
                saveImage(imageUri)
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun saveImage(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
            .child("imagenes/${imageUri.lastPathSegment}")
        Toast.makeText(requireContext(), "Actualizado Perfil ... aguarde un instante.", Toast.LENGTH_SHORT).show()
        storageReference.putFile(imageUri).addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                userAccountDTO.profileImage = downloadUri.toString()
                userAccountRepository.update(userAccountDTO) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Perfil Actualizado", Toast.LENGTH_SHORT).show()
                        renderProfileImage()
                    } else {
                        Log.e("UserProfileFragment", "Error al actualizar el usuario en Firestore")
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al guardar la imagen.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUserInfo(userAccountDTO: UserAccountDTO) {
        val totalPoints = userAccountDTO.challengesAmount * userAccountDTO.accumulatedScore

        binding.userNameTextView.text = getString(R.string.concatenate_two_string, userAccountDTO.name, userAccountDTO.surname)
        binding.scoreTextView.text = getString(R.string.score_label, userAccountDTO.accumulatedScore.toString())
        renderProfileImage()
    }

    private fun renderProfileImage() {
        Picasso.get().load(userAccountDTO.profileImage).into(binding.userImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
