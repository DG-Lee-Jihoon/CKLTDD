import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.flashcardapp.data.Card

class FirebaseSync {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Hàm này để đẩy thẻ từ máy lên kho Google
    fun uploadCard(card: Card) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .collection("cards").document(card.id.toString())
                .set(card) // Đẩy nguyên cục dữ liệu thẻ lên
        }
    }

    // Hàm này để máy B "ngóng" xem trên kho có gì mới thì tải về
    fun listenFromCloud(onCardReceived: (Card) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .collection("cards")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener
                    for (doc in snapshots!!) {
                        val card = doc.toObject(Card::class.java)
                        onCardReceived(card)
                    }
                }
        }
    }
}