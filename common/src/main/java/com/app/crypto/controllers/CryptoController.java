package com.app.crypto.controllers;
import com.app.crypto.interfaces.Crypto;
import com.app.crypto.views.CryptoView;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.controllers.FormController;
import com.codename1.ui.Form;
import com.codename1.ui.layouts.BorderLayout;

public class CryptoController extends FormController {

    public CryptoController(Controller parent, Node viewNode) {
        super(parent);
        Form editProfileForm = new Form(new BorderLayout());
        editProfileForm.getToolbar().hideToolbar();
        editProfileForm.setFormBottomPaddingEditingMode(true);
        Entity entity = new Entity();
        editProfileForm.add(BorderLayout.CENTER, new CryptoView(entity,viewNode));
        setView(editProfileForm);
    }

}
