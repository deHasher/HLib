package net.dehasher.hlib.database;

import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Tools;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import net.dehasher.hlib.Informer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Redis {
    private boolean isEnabled = true;
    private final JedisPool poolDefault;
    @Getter
    private static final Set<JedisPubSub> pubSubs = ConcurrentHashMap.newKeySet();

    public Redis(String address, String password) {
        String ip = address.split(":")[0];
        int port  = (!address.contains(":")) ? Tools.parseInt(address.split(":")[1]) : 6379;

        JedisPoolConfig config = new JedisPoolConfig();

        if (!password.isEmpty()) {
            poolDefault = new JedisPool(config, ip, port, 0, password);
        } else {
            poolDefault = new JedisPool(config, ip, port, 0);
        }
    }

    public JedisPool getPool() {
        try {
            return getPoolDefault();
        } catch (Throwable t) {
            shutdown();
            Informer.send("Failed to connect to the Redis! Check your configuration...");
            return null;
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.publish(channel, message);
        } catch (Throwable ignored) {
            shutdown();
        }
    }

    public void shutdown() {
        setEnabled(false);
        getPubSubs().stream()
                .filter(JedisPubSub::isSubscribed)
                .forEach(JedisPubSub::unsubscribe);
        getPoolDefault().close();
    }

    public static class PubSub extends JedisPubSub {
        public PubSub() {
            Redis.getPubSubs().add(this);
        }
    }

    /* Пример создания канала обмена сообщениями и отправки в него сообщений:

            Scheduler.doAsync(() -> {
                try (Jedis jedis = Tools.getRedis().getPubSubPool().getResource()) {
                    jedis.subscribe(new Redis.PubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            try {
                                // Код...
                            } catch (Throwable t) {
                                // Важно отлавливать ошибки практически всегда!
                                // Если что-то сломается то хера с два Jedis об этом сообщит правильно...
                                Informer.send("Error!");
                                t.printStackTrace();
                            }
                        }
                    }, plugin.class.getSimpleName());
                }
            });

            Tools.getRedis().publish(plugin.class.getSimpleName(), "i love your mom");

    // Пример вызова любой другой функции Redis'a:

            try (Jedis jedis = Tools.getRedis().getPool().getResource()) {
                jedis.zadd("top-players", 5, "test");
            }

    */
}