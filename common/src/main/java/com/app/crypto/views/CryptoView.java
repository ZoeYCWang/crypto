package com.app.crypto.views;

import com.app.crypto.CryptoUtils;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.rad.controllers.FormController;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.ui.*;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.NumericConstraint;
import com.codename1.ui.validation.Validator;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class CryptoView extends AbstractEntityView{

    Node viewNode;

    public CryptoView(Entity entity, Node viewNode) {
        super(entity);
        this.viewNode = viewNode;
        // 完全对标样例：根布局BorderLayout + 居中行为
        setLayout(new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE));
        // 垂直滚动容器（对标样例wrapper）
        Container wrapper = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        wrapper.setScrollVisible(false);
        wrapper.setScrollableY(true);
        setUIID("Crypto"); // 对标样例EditProfile UIID

        // ========== 头部：返回按钮 + 标题（完全照搬样例格式） ==========
        Button backButton = new Button(FontImage.MATERIAL_KEYBOARD_ARROW_LEFT);
        backButton.setUIID("CryptoBackButton"); // 对标样例EditProfileBackButton
        backButton.addActionListener(evt -> {
            evt.consume();
            ActionSupport.dispatchEvent(new FormController.FormBackEvent(backButton));
        });
        Label headerLabel = new Label("Richard Pro", "CryptoHeaderLabel"); // 对标样例HeaderLabel
        Container headerCnt = BorderLayout.center(headerLabel).add(BorderLayout.WEST, backButton);
        headerCnt.setUIID("CryptoHeaderCnt"); // 对标样例HeaderCnt
        add(BorderLayout.NORTH, headerCnt);

        // ===================== 【优化后】文本密码：标签前置 + 输入框 =====================
        Label labelTextPwd = new Label("字典：");
        labelTextPwd.setUIID("SignUpFieldHint");
        TextField textPassword = new TextField("","至少八位", 20, TextArea.ANY);
        textPassword.setUIID("EditProfileField");
        Container textPwdContainer = BoxLayout.encloseX(labelTextPwd, textPassword);
//        textPwdContainer.setGap(8);

        // ===================== 【优化后】数字密码：标签前置 + 密码框 + 眼睛切换 =====================
        Label labelNumberPwd = new Label("数字密码：");
        labelNumberPwd.setUIID("SignUpFieldHint");
        // 改为密码隐藏输入框
        TextField numberPassword = new TextField("","至少六位", 10, TextArea.PASSWORD);
        numberPassword.setUIID("EditProfileField");
        // 眼睛切换按钮
// 眼睛切换按钮（修复版，能正常显示）
        Button btnEye = new Button();
// 创建可见的眼睛图标（固定大小+颜色，不依赖外部）
        btnEye.setIcon(FontImage.createMaterial(FontImage.MATERIAL_VISIBILITY_OFF,labelNumberPwd.getUnselectedStyle()));
// 去掉错误的Label样式
        btnEye.setUIID("");
// 正常宽高，不压扁
        btnEye.setPreferredSize(new Dimension(100, 40));
        // 眼睛点击切换明文/密文
        btnEye.addActionListener(e -> {
            // 正确判断：当前是否是密码模式
            boolean isPasswordMode = numberPassword.getConstraint() == TextArea.PASSWORD;

            if (isPasswordMode) {
                // 切换为：显示明文
                numberPassword.setConstraint(TextArea.ANY);
                btnEye.setIcon(FontImage.createMaterial(FontImage.MATERIAL_VISIBILITY, labelNumberPwd.getUnselectedStyle()));
            } else {
                // 切换为：隐藏密码
                numberPassword.setConstraint(TextArea.PASSWORD);
                btnEye.setIcon(FontImage.createMaterial(FontImage.MATERIAL_VISIBILITY_OFF, labelNumberPwd.getUnselectedStyle()));
            }
            numberPassword.repaint();
        });
        Container numberPwdContainer = BoxLayout.encloseX(labelNumberPwd, numberPassword, btnEye);
//        numberPwdContainer.setGap(8);

        // ========== 1. 明文输入区域（标签+文本域，对标样例输入框样式） ==========
        Label plainTextLabel = new Label("明文:");
        plainTextLabel.setUIID("SignUpFieldHint"); // 沿用样例提示文字样式
        Button plainButton = new Button("复制明文");
        plainButton.setUIID("Button");
        Container plainTextContainer = BoxLayout.encloseX(plainTextLabel, plainButton);

        TextArea plainTextArea = new TextArea("", 8, 20, TextArea.ANY);
        plainTextArea.setUIID("EditProfileField"); // 沿用样例输入框样式
        plainTextArea.setVerticalAlignment(Component.TOP);
        plainTextArea.setGrowByContent(true); // 多行自适应


        // ========== 2. 加密/解密按钮区域（水平并排，对标样例SaveButton样式） ==========
        Button encryptButton = new Button("加密文本", "CryptoButton"); // 对标样例EditProfileSaveButton
        Button decryptButton = new Button("解密文本", "decryptButton");
        encryptButton.setUIID("Button");
        decryptButton.setUIID("Button");
        // 水平容器包裹两个按钮（样例风格）
        Container buttonsContainer = BoxLayout.encloseX(encryptButton, decryptButton);
        buttonsContainer.getStyle().setMargin(20,20,0,0);

        // ========== 3. 密文输入区域（标签+文本域，完全对标上文） ==========
        Label cipherTextLabel = new Label("密文:");
        cipherTextLabel.setUIID("SignUpFieldHint");
        Button cipherButton = new Button("复制密文");
        cipherButton.setUIID("Button");
        Container cipherTextContainer = BoxLayout.encloseX(cipherTextLabel, cipherButton);

        TextArea cipherTextArea = new TextArea("", 8, 20, TextArea.ANY);
        cipherTextArea.setUIID("EditProfileField");
        cipherTextArea.setGrowByContent(true);
        cipherTextArea.setVerticalAlignment(Component.TOP);


        // ========== 所有内容垂直打包进wrapper（替换为优化后的密码容器） ==========
        Container allContent = BoxLayout.encloseY(
                textPwdContainer,      // 优化后的文本密码
                numberPwdContainer,    // 优化后的数字密码
                plainTextContainer,
                plainTextArea,
                buttonsContainer,
                cipherTextContainer,
                cipherTextArea
        );
        allContent.setScrollVisible(false);
        allContent.setScrollableY(false);
        wrapper.add(allContent);

        Validator validator = new Validator();
        validator.addConstraint(textPassword, new LengthConstraint(8));
        validator.addConstraint(numberPassword, new LengthConstraint(6));
        validator.addSubmitButtons(encryptButton, decryptButton);


        // ========== 按钮点击事件（预留加密/解密逻辑位，对标样例SaveButton） ==========
        encryptButton.addActionListener(evt -> {
            evt.consume();
            if (!validator.isValid()) return; // 🔥 仅1行

            // 加密逻辑：获取plainTextArea明文 → 加密 → 赋值给cipherTextArea密文
            String plainText = plainTextArea.getText();
            String pwd1 = textPassword.getText();
            String pwd2 = numberPassword.getText();
            if (plainText == null || plainText.isEmpty()) {
                Dialog.show("错误", "明文不能为空", "确定", null);
                return;
            }
            try {
                String encrypt = CryptoUtils.encrypt(plainText,getPwd(pwd1,pwd2));
                cipherTextArea.setText(encrypt);
            } catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "";
                }
                if (message.length() > 10){
                    message = message.substring(0, 10) + "...";
                }
                Dialog.show("错误", message, "确定", null);

            }
        });

        decryptButton.addActionListener(evt -> {
            evt.consume();
            if (!validator.isValid()) return; // 🔥 仅1行
            // 解密逻辑：获取cipherTextArea密文 → 解密 → 赋值给plainTextArea明文
            String cipherText = cipherTextArea.getText();
            if (cipherText == null || cipherText.isEmpty()) {
                Dialog.show("错误", "密文不能为空", "确定", null);
                return;
            }
            String pwd1 = textPassword.getText();
            String pwd2 = numberPassword.getText();
            try {
                String encrypt = CryptoUtils.decrypt(cipherText,getPwd(pwd1,pwd2));
                plainTextArea.setText(encrypt);
            }catch (InvalidCipherTextException invalidCipherTextException){
                Dialog.show("错误", "格式错误", "确定", null);
            }catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "";
                }
                if (message.length() > 10){
                    message = message.substring(0, 10) + "...";
                }
                Dialog.show("错误", message, "确定", null);
            }
            // plainTextArea.setText(解密结果);
        });

        // ========== 🔥 复制按钮事件 ==========
        plainButton.addActionListener(evt -> {
            evt.consume();
            String content = plainTextArea.getText();
            if(content != null && !content.isEmpty()){
                Display.getInstance().copyToClipboard(content);
                Dialog.show("提示","复制成功！",null,null,Dialog.TYPE_INFO,null,1000 );
            } else {
                Dialog.show("错误","暂无明文可复制！",null,null,Dialog.TYPE_ERROR,null,1000 );
            }
        });


        // ========== 🔥 复制按钮事件 ==========
        cipherButton.addActionListener(evt -> {
            evt.consume();
            String content = cipherTextArea.getText();
            if(content != null && !content.isEmpty()){
                Display.getInstance().copyToClipboard(content);
                Dialog.show("提示","复制成功！",null,null,Dialog.TYPE_INFO,null,1000 );
            } else {
                Dialog.show("错误","暂无密文可复制！",null,null,Dialog.TYPE_ERROR,null,1000 );
            }
        });

        // 对标样例：容器放入CENTER
        add(BorderLayout.CENTER, wrapper);
    }


    private String getPwd(String pwd1, String pwd2) {
        return pwd1.trim() + pwd2.trim();

    }

    // 对标样例：保留空实现方法
    @Override
    public void update() {}

    @Override
    public void commit() {}

    @Override
    public Node getViewNode() {
        return viewNode;
    }
}