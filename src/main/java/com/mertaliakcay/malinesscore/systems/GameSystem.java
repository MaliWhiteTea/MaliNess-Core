package com.mertaliakcay.malinesscore.systems;

import com.mertaliakcay.malinesscore.MaliNessCore;

/**
 * Her oyun sistemi (tpa, home, rtp vb.) bu arayüzü uygular.
 * Yeni sistemler için AbstractGameSystem sınıfından türetmek önerilir;
 * böylece configs/&lt;sistem&gt;.yml ve langs/&lt;sistem&gt;.yml otomatik yüklenir.
 */
public interface GameSystem {

    String getName();

    void enable(MaliNessCore plugin);

    void disable();
}
