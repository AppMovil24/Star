import android.content.Context
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class FCMNotificationSender(
    private val userToken: String,
    private val title: String,
    private val message: String,
    private val serverKey: String,
    private val senderId: String
) {

    fun sendNotification(context: Context) {
        val requestQueue = Volley.newRequestQueue(context) // 'context' debe ser el contexto de tu aplicación

        val jsonBody = JSONObject()
        jsonBody.put("to", userToken)
        jsonBody.put("priority", "high")

        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", message)
        jsonBody.put("notification", notification)

        val stringRequest = object : StringRequest(
            Request.Method.POST, "https://fcm.googleapis.com/fcm/send",
            Response.Listener<String> { response ->
                Toast.makeText(
                    context,
                    "Respuesta: ${response.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    context,
                    "Error: ${error.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "key=$serverKey"
                headers["Content-Type"] = "application/json"
                headers["Sender"] = "id=$senderId"
                return headers
            }

            override fun getBody(): ByteArray {
                return jsonBody.toString().toByteArray()
            }
        }

        requestQueue.add(stringRequest)
    }
}
