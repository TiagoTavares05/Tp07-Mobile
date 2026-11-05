# AppPerfilUsuario - Autenticação e Perfil com FirebaseUI

Projeto Android escrito em Kotlin para autenticação e cadastro de usuários baseado na documentação oficial do FirebaseUI (`FirebaseAuthUIActivityResultContract`, `AuthUI` e `FirebaseAuth`).

## Estrutura do Aplicativo
- **`LoginActivity`**: Tela inicial de login com campos de Nome, E-mail e Senha, opção para criar novo usuário (`createUserWithEmailAndPassword`), login direto (`signInWithEmailAndPassword`) e integração com o `AuthUI.getInstance().createSignInIntentBuilder()`.
- **`MainActivity`**: Tela de perfil exibida ao acessar o aplicativo, apresentando os dados da conta autenticada e o botão **Sair da Conta** (`AuthUI.getInstance().signOut(this)`), que desconecta o usuário e retorna para a tela de login.
