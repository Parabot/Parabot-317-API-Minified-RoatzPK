package org.rev317.min;

import java.awt.event.*;
import org.parabot.core.Context;
import org.parabot.core.Core;
import org.parabot.core.Directories;
import org.parabot.core.asm.ASMClassLoader;
import org.parabot.core.asm.adapters.AddInterfaceAdapter;
import org.parabot.core.asm.hooks.HookFile;
import org.parabot.core.desc.ServerProviderInfo;
import org.parabot.core.reflect.RefClass;
import org.parabot.core.reflect.RefField;
import org.parabot.core.reflect.RefMethod;
import org.parabot.core.ui.components.GamePanel;
import org.parabot.core.ui.components.VerboseLoader;
import org.parabot.environment.api.utils.WebUtil;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.servers.ServerManifest;
import org.parabot.environment.servers.ServerProvider;
import org.parabot.environment.servers.Type;
import org.rev317.min.accessors.Client;
import org.rev317.min.script.ScriptEngine;
import org.rev317.min.ui.BotMenu;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * @author Everel, JKetelaar
 */
@ServerManifest(author = "Everel & JKetelaar", name = "Server name here", type = Type.INJECTION, version = 2.1)
public class Loader extends ServerProvider {
    private boolean extended = true;

    public static Client getClient() {
        return (Client) Context.getInstance().getClient();
    }

    @Override
    public Applet fetchApplet() {
        try {
            final Context        context     = Context.getInstance();
            final ASMClassLoader classLoader = context.getASMClassLoader();
            final Class<?>       clientClass = classLoader.loadClass(Context.getInstance().getServerProviderInfo().getClientClass());

            Object instance = clientClass.newInstance();

            Field field = instance.getClass().getSuperclass().getDeclaredField("Y");
            field.setAccessible(true);

            Class<?>       frameClass    = classLoader.loadClass("com.b.a.E");
            Constructor<?> s             = frameClass.getConstructors()[0];
            s.setAccessible(true);
            Frame         frameInstance = (Frame) s.newInstance(instance, 765, 503, false, false);

            Field field2 = instance.getClass().getSuperclass().getDeclaredField("ai");

            RefField x = new RefField(field2, instance);
            x.setBoolean(true);

            System.out.println(instance.getClass().getSuperclass().getDeclaredField("ai").get(instance));

            field.set(instance, frameInstance);

            return (Applet) instance;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public URL getJar() {
        ServerProviderInfo serverProvider = Context.getInstance().getServerProviderInfo();

        File target = new File(Directories.getCachePath(), serverProvider.getClientCRC32() + ".jar");
        if (!target.exists()) {
            WebUtil.downloadFile(serverProvider.getClient(), target, VerboseLoader.get());
        }

        return WebUtil.toURL(target);
    }

    @Override
    public void addMenuItems(JMenuBar bar) {
        new BotMenu(bar);
    }

    @Override
    public void injectHooks() {
        AddInterfaceAdapter.setAccessorPackage("org/rev317/min/accessors/");
        try {
            super.injectHooks();
        } catch (Exception e) {
            if (Core.inVerboseMode()) {
                e.printStackTrace();
            }
            this.extended = false;
            super.injectHooks();
        }
    }

    @Override
    public void initScript(Script script) {
        ScriptEngine.getInstance().setScript(script);
        ScriptEngine.getInstance().init();
    }

    @Override
    public HookFile getHookFile() {
        if (this.extended) {
            return new HookFile(Context.getInstance().getServerProviderInfo().getExtendedHookFile(), HookFile.TYPE_XML);
        } else {
            return new HookFile(Context.getInstance().getServerProviderInfo().getHookFile(), HookFile.TYPE_XML);
        }
    }

    public void unloadScript(Script script) {
        ScriptEngine.getInstance().unload();
    }

    @Override
    public void init() {
    }

    @Override
    public void preAppletInit() {

    }

    @Override
    public void postAppletStart() {

        GamePanel panel = GamePanel.getInstance();
        Applet applet = (Applet) Context.getInstance().getClient();
        try {
            RefClass appletClass = new RefClass(Context.getInstance().getASMClassLoader().loadClass("com.b.a.B"), applet);
            final RefField frameField = appletClass.getField("Y"); // frame
            RefField graphicsField = appletClass.getField("X");
            RefField useApplet = appletClass.getField("ai");
            useApplet.setBoolean(true);
           // System.out.println("check: "+useApplet.asBoolean());
            Frame frame = (Frame) frameField.asObject();
            frame.dispose();
            frameField.set(null);
            RefMethod componentMethod = appletClass.getMethod("o");
            Component component = (Component) componentMethod.invoke();
            //System.out.println("component: "+component);
            panel.add(component);
            applet.repaint();
            applet.paintAll(applet.getGraphics());
            graphicsField.set(applet.getGraphics());
            component.addMouseListener((MouseListener)applet);
            component.addMouseMotionListener((MouseMotionListener)applet);
            component.addKeyListener((KeyListener)applet);
            component.addFocusListener((FocusListener)applet);
            component.addMouseWheelListener((MouseWheelListener)applet);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        panel.validate();
    }
}