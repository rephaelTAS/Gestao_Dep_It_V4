package com.ossobo.gestaoDepIt.controllers.login;


import javafx.event.ActionEvent;

public class MusarSenha {
    public void cadastroSenha(ActionEvent e){
        //if (pasword.getText().equals(confi.getText())){
            mudaSenha();
       // }
    }

    public void mudaSenha(){


        String nome = "Rafa";
        String senha = "1234";
        String confiSenha = "1234";

        String insertFiles = " UPDATE usuarios SET senha = '" + senha + "' WHERE nome = '" + nome + "';";



    }
}
