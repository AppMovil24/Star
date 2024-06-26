package com.appmovil24.starproyect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.databinding.ActivityRegisterUserBinding
import com.appmovil24.starproyect.databinding.FragmentHomeBinding
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class RegisterUserFormActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val PICK_IMAGE_REQUEST = 2
    }

    private lateinit var userAccountRepository: UserAccountRepository
    private lateinit var newUserAccountDTO: UserAccountDTO
    private lateinit var binding: ActivityRegisterUserBinding
    private var isImageLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        var auth = FirebaseAuth.getInstance()
        userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser)

        newUserAccountDTO = UserAccountDTO(
            id = "",
            name = "",
            surname = "",
            email = auth.currentUser?.email ?: "",
            isSupervisor = false,
            accumulatedScore = 0,
            challengesAmount = 0
        )

        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
        binding.imageGaleryButton.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST)
        }

        binding.registerUserRegisterButton.setOnClickListener {
            val username = binding.registerUserUsername.text.toString().trim()
            userAccountRepository.exists(username) { existe ->
                if (!existe) {
                    newUserAccountDTO = UserAccountDTO(
                        id = username,
                        name = binding.registerUserRealName.text.toString().trim(),
                        surname = binding.registerUserSurname.text.toString().trim(),
                        email = auth.currentUser?.email ?: "",
                        isSupervisor = binding.registerUserSupervisor.isChecked,
                        accumulatedScore = 0,
                        challengesAmount = 0,
                        profileImage = newUserAccountDTO.profileImage
                    )
                    createUser()
                } else {
                    Toast.makeText(this, "El usuarioId ya está en uso, elige otro.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.userImage.setImageBitmap(imageBitmap)
            val imageUri = getImageUri(this, imageBitmap)
            saveImage(imageUri)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if(imageUri != null)
                saveImage(imageUri)
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun saveImage(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
            .child("imagenes/${imageUri.lastPathSegment}")
        Toast.makeText(this, "Actualizado Perfil ... aguarde un instante.", Toast.LENGTH_SHORT).show()
        storageReference.putFile(imageUri).addOnSuccessListener {
            isImageLoading = true
            storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                newUserAccountDTO.profileImage = downloadUri.toString()
                Picasso.get().load(newUserAccountDTO.profileImage).into(findViewById<ImageView>(R.id.user_image))
                isImageLoading = false
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al guardar la imagen.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUser() {
        if(isImageLoading)
            Toast.makeText(this, "Reintente en un momento. La imagen se esta cargando.", Toast.LENGTH_SHORT).show()
        else
            userAccountRepository.add(newUserAccountDTO) { success ->
                if (success) {
                    Log.d("RegisterActivity", "Usuario registrado correctamente")
                    val intent = Intent(this@RegisterUserFormActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("RegisterActivity", "Error al registrar el usuario en Firestore")
                }
            }
    }
}
