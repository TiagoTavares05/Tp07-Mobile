package br.universidade.appperfilusuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.universidade.appperfilusuario.databinding.ActivityLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private val loginLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        tratarResultadoFirebaseUi(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val preferencias = getSharedPreferences("cadastro_usuarios", Context.MODE_PRIVATE)
        val emailAtivo = preferencias.getString("email_ativo", null)
        if (firebaseAuth.currentUser != null || emailAtivo != null) {
            irParaPerfil()
            return
        }

        binding.btnAcessar.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val senha = binding.edtSenhaLogin.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Informe seu e-mail e senha!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val atual = firebaseAuth.currentUser
                        preferencias.edit()
                            .putString("email_ativo", atual?.email ?: email)
                            .putString("nome_ativo", atual?.displayName ?: preferencias.getString("user_$email", "Usuário"))
                            .apply()
                        Toast.makeText(this, "Acesso liberado!", Toast.LENGTH_SHORT).show()
                        irParaPerfil()
                    } else {
                        val senhaGravada = preferencias.getString("pass_$email", null)
                        if (senhaGravada != null && senhaGravada == senha) {
                            val nomeGravado = preferencias.getString("user_$email", "Usuário")
                            preferencias.edit()
                                .putString("email_ativo", email)
                                .putString("nome_ativo", nomeGravado)
                                .apply()
                            Toast.makeText(this, "Acesso liberado!", Toast.LENGTH_SHORT).show()
                            irParaPerfil()
                        } else {
                            Toast.makeText(this, "Dados incorretos ou usuário não cadastrado!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        binding.btnCriarConta.setOnClickListener {
            val nome = binding.edtNomeCadastro.text.toString().trim()
            val email = binding.edtEmailLogin.text.toString().trim()
            val senha = binding.edtSenhaLogin.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha para criar a conta!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha.length < 6) {
                binding.edtSenhaLogin.error = "Mínimo de 6 caracteres"
                return@setOnClickListener
            }

            val nomeUsuario = if (nome.isNotEmpty()) nome else email.substringBefore("@")

            preferencias.edit()
                .putString("pass_$email", senha)
                .putString("user_$email", nomeUsuario)
                .putString("email_ativo", email)
                .putString("nome_ativo", nomeUsuario)
                .apply()

            firebaseAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val profileRequest = UserProfileChangeRequest.Builder()
                            .setDisplayName(nomeUsuario)
                            .build()
                        firebaseAuth.currentUser?.updateProfile(profileRequest)
                    }
                    Toast.makeText(this, "Cadastro concluído!", Toast.LENGTH_SHORT).show()
                    irParaPerfil()
                }
        }

        binding.btnLoginFirebaseUi.setOnClickListener {
            abrirFirebaseUi()
        }
    }

    private fun abrirFirebaseUi() {
        val provedores = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        val intentLogin = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(provedores)
            .build()

        loginLauncher.launch(intentLogin)
    }

    private fun tratarResultadoFirebaseUi(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val usuario = FirebaseAuth.getInstance().currentUser
            val preferencias = getSharedPreferences("cadastro_usuarios", Context.MODE_PRIVATE)
            preferencias.edit()
                .putString("email_ativo", usuario?.email ?: "usuario@email.com")
                .putString("nome_ativo", usuario?.displayName ?: "Usuário")
                .apply()
            irParaPerfil()
        } else {
            Toast.makeText(this, "Login via FirebaseUI não concluído.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun irParaPerfil() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
