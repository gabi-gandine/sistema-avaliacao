package com.forms.mavenproject1;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.sql.*;

public class RegistrationForm implements ActionListener {
    JFrame frame;
    JLabel nameLabel=new JLabel("Nome");
    JLabel emailLabel=new JLabel("Email");
    JLabel matriculaLabel=new JLabel("Matricula Semestre");
    JLabel passwordLabel=new JLabel("PASSWORD");
    JLabel confirmPasswordLabel=new JLabel("CONFIRM PASSWORD");
    JTextField nameTextField=new JTextField();
    JTextField matriculaField=new JTextField();
    JPasswordField passwordField=new JPasswordField();
    JPasswordField confirmPasswordField=new JPasswordField();
    JTextField emailTextField=new JTextField();
    JButton registerButton=new JButton("REGISTER");
    JButton resetButton=new JButton("RESET");


    RegistrationForm()
    {
        createWindow();
        setLocationAndSize();
        addComponentsToFrame();
        actionEvent();
    }
    public void createWindow()
    {
        frame=new JFrame();
        frame.setTitle("Registration Form");
        frame.setBackground(Color.CYAN);
        frame.setBounds(40,40,380,600);
        frame.getContentPane().setBackground(Color.pink);
        frame.getContentPane().setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(new JTextArea());
    }
    public void setLocationAndSize()
    {
        nameLabel.setBounds(20,20,40,70);
        nameLabel.setVisible(true);
        matriculaLabel.setBounds(20,70,80,70);
        passwordLabel.setBounds(20,170,100,70);
        confirmPasswordLabel.setBounds(20,220,140,70);
        emailLabel.setBounds(20,320,100,70);
        nameTextField.setBounds(180,43,165,23);
        matriculaField.setBounds(180,93,165,23);
        passwordField.setBounds(180,193,165,23);
        confirmPasswordField.setBounds(180,243,165,23);
        emailTextField.setBounds(180,343,165,23);
        registerButton.setBounds(70,400,100,35);
        resetButton.setBounds(220,400,100,35);
    }
    public void addComponentsToFrame()
    {
        System.out.println("add");
        frame.add(nameLabel);
        frame.add(matriculaLabel);
        frame.add(passwordLabel);
        frame.add(confirmPasswordLabel);
        frame.add(emailLabel);
        frame.add(nameTextField);
        frame.add(matriculaField);
        frame.add(passwordField);
        frame.add(confirmPasswordField);
        frame.add(emailTextField);
        frame.add(registerButton);
        frame.add(resetButton);
        frame.setVisible(true);
    }
    public void actionEvent()
    {
        registerButton.addActionListener(this);
        resetButton.addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==registerButton)
        {
            //try {
                /*Pstatement.setString(1,nameTextField.getText());
                Pstatement.setString(2,matriculaField.getText());
                Pstatement.setString(4,passwordField.getPassword().toString());
                Pstatement.setString(5,confirmPasswordField.getPassword().toString());
                Pstatement.setString(7,emailTextField.getText());*/
                if(passwordField.getPassword().equals(confirmPasswordField.getPassword()))
                {

                    //Pstatement.executeUpdate();
                    JOptionPane.showMessageDialog(null,"Data Registered Successfully");
                }
                else
                {
                    JOptionPane.showMessageDialog(null,"password did not match");
                }

           // } catch ( ) {
               //e1.printStackTrace();
          //  }


        }
        if(e.getSource()==resetButton)
        {
            nameTextField.setText("");
            matriculaField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            emailTextField.setText("");
        }

    }
}
