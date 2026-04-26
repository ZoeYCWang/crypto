package com.app.crypto;

import com.codename1.system.Lifecycle;
import com.codename1.ui.Display;
import com.codename1.ui.Form;


import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;

public class cryptoStub {
    public static void main(String[] args) {
        System.out.println("=== 桌面端开始启动 ===");

        // ==============================================
        // ✅ 核心：官方公开写法！传null初始化桌面环境
        // 彻底解决：资源加载空指针（不用JavaSEPort！）
        // ==============================================
        Display.init(null);

        // ==============================================
        // ✅ 切EDT线程，解决EDT违规
        // ==============================================
        Display.getInstance().callSerially(() -> {
            crypto app = new crypto();
            app.init(null);  // 环境已就绪，加载主题绝不空指针
            app.start();     // 正常显示页面
            System.out.println("=== 启动成功！===");
        });

    }
}
