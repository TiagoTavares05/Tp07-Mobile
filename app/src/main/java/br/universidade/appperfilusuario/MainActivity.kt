package br.universidade.appperfilusuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.universidade.appperfilusuario.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val preferencias = getSharedPreferences("cadastro_usuarios", Context.MODE_PRIVATE)

        val usuarioAtual = firebaseAuth.currentUser
        val nomeExibido = usuarioAtual?.displayName?.takeIf { it.isNotBlank() }
            ?: preferencias.getString("nome_ativo", "Usuário")
        val emailExibido = usuarioAtual?.email?.takeIf { it.isNotBlank() }
            ?: preferencias.getString("email_ativo", "email@exemplo.com")

        binding.tvNomePerfil.text = nomeExibido
        binding.tvEmailPerfil.text = emailExibido

        binding.btnDeslogar.setOnClickListener {
            preferencias.edit()
                .remove("email_ativo")
                .remove("nome_ativo")
                .apply()

            firebaseAuth.signOut()

            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    Toast.makeText(this, "Sessão finalizada!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        }
    }
}
