package de.dytanic.cloudnet.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.management.OperatingSystemMXBean;
import de.dytanic.cloudnet.lib.network.protocol.codec.ProtocolInDecoder;
import de.dytanic.cloudnet.lib.network.protocol.codec.ProtocolLengthDeserializer;
import de.dytanic.cloudnet.lib.network.protocol.codec.ProtocolLengthSerializer;
import de.dytanic.cloudnet.lib.network.protocol.codec.ProtocolOutEncoder;
import de.dytanic.cloudnet.lib.utility.Acceptable;
import de.dytanic.cloudnet.lib.utility.Catcher;
import de.dytanic.cloudnet.lib.utility.document.Document;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64Decoder;
import io.netty.handler.codec.base64.Base64Encoder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Tareko on 24.05.2017.
 */
public final class NetworkUtils {

    private NetworkUtils()
    {
    }

    public static final boolean EPOLL = Epoll.isAvailable();

    public static final Gson GSON = new Gson();

    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##");

    public static final String
            DEV_PROPERTY = "_CLOUDNET_DEV_SERVICE_UNIQUEID_",
            EMPTY_STRING = "",
            SPACE_STRING = " ",
            SLASH_STRING = "/";

    public static Class<? extends SocketChannel> socketChannel()
    {
        return EPOLL ? EpollSocketChannel.class : NioSocketChannel.class;
    }

    private static final char[] values = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'
            , 'J', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '0', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'};

    public static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex)
        {
            return "127.0.0.1";
        }
    }

    public static double cpuUsage()
    {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 100;
    }

    public static double internalCpuUsage()
    {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100;
    }

    public static long systemMemory()
    {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    }

    public static TypeToken<CloudNetwork> cloudnet()
    {
        return new TypeToken<CloudNetwork>() {
        };
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannel()
    {
        return EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static EventLoopGroup eventLoopGroup()
    {
        return EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    public static EventLoopGroup eventLoopGroup(int threads)
    {
        return EPOLL ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    public static EventLoopGroup eventLoopGroup(ThreadFactory threadFactory)
    {
        return EPOLL ? new EpollEventLoopGroup(0, threadFactory) : new NioEventLoopGroup(0, threadFactory);
    }

    public static EventLoopGroup eventLoopGroup(int threads, ThreadFactory threadFactory)
    {
        return EPOLL ? new EpollEventLoopGroup(threads, threadFactory) : new NioEventLoopGroup(threads, threadFactory);
    }

    public static <T> void addAll(Collection<T> key, Collection<T> value)
    {
        for (T k : value)
            key.add(k);
    }

    public static <T, V> void addAll(java.util.Map<T, V> key, java.util.Map<T, V> value)
    {
        for (T key_ : value.keySet())
            key.put(key_, value.get(key_));
    }

    public static void addAll(Document key, Document value)
    {
        for (String keys : value.keys())
            key.append(keys, value.get(keys));
    }

    public static <T, V> void addAll(java.util.Map<T, V> map, List<V> list, Catcher<T, V> catcher)
    {
        for (V ke : list)
            map.put(catcher.doCatch(ke), ke);
    }

    public static <T, V> void addAll(java.util.Map<T, V> key, java.util.Map<T, V> value, Acceptable<V> handle)
    {
        for (T key_ : value.keySet())
            if (handle.isAccepted(value.get(key_))) key.put(key_, value.get(key_));
    }

    public static Channel initChannel(Channel channel)
    {
        channel.pipeline()
                .addLast(
                        new ProtocolLengthDeserializer(),
                        new ProtocolInDecoder(), //
                        new Base64Decoder(),
                        new ProtocolLengthSerializer(),
                        new ProtocolOutEncoder(), //
                        new Base64Encoder());
        return channel;
    }

    public static boolean checkIsNumber(String input)
    {
        try
        {
            Integer.parseInt(input);
            return true;
        } catch (Exception ex)
        {
            return false;
        }
    }

    public static ConnectableAddress fromString(String input)
    {
        String[] x = input.split(":");
        return new ConnectableAddress(x[0], Integer.parseInt(x[1]));
    }

    public static String randomString(int size)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (short i = 0; i < size; i++)
            stringBuilder.append(values[RANDOM.nextInt(values.length)]);
        return stringBuilder.substring(0);
    }

    public static void writeWrapperKey()
    {
        Random random = new Random();

        Path path = Paths.get("WRAPPER_KEY.cnd");
        if (!Files.exists(path))
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (short i = 0; i < 4096; i++)
                stringBuilder.append(values[random.nextInt(values.length)]);

            try
            {
                Files.createFile(path);
                try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))
                {
                    outputStreamWriter.write(stringBuilder.substring(0) + "\n");
                    outputStreamWriter.flush();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    public static String readWrapperKey()
    {
        Path path = Paths.get("WRAPPER_KEY.cnd");
        if (!Files.exists(path)) return null;

        StringBuilder builder = new StringBuilder();

        try
        {
            for (String string : Files.readAllLines(path, StandardCharsets.UTF_8))
                builder.append(string);
            return builder.substring(0);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return builder.substring(0);
    }

    public static void sleepUninterruptedly(long time)
    {
        try
        {
            Thread.sleep(time);
        } catch (InterruptedException e)
        {
        }
    }

    public static <K, V> HashMap<K, V> newHashMap()
    {
        return new HashMap<>();
    }

    public static ConcurrentHashMap newConcurrentHashMap()
    {
        return new ConcurrentHashMap();
    }

    public static void header()
    {
        System.out.println(NetworkUtils.SPACE_STRING);
        /*
        System.out.println("██████ █      ██████ █   █ █████ ██    █ █████ █████ [" + NetworkUtils.class.getPackage().getImplementationVersion() + "]");
        System.out.println("█R     █E     █Z   █ █S  █ █Y  █ █M█   █ █       █");
        System.out.println("█      █      █    █ █   █ █   █ █  █  █ ████    █");
        System.out.println("█D     █Y     █T   █ █A  █ █N  █ █   █I█ █C      █");
        System.out.println("██████ ██████ ██████ █████ █████ █    ██ ████    █");
        */
        System.out.println(" _______         _____  _     _ ______  __   _ _______ _______ [" + NetworkUtils.class.getPackage().getImplementationVersion() + "]");
        System.out.println(" |       |      |     | |     | |     \\ | \\  | |______    |   ");
        System.out.println(" |_____  |_____ |_____| |_____| |_____/ |  \\_| |______    |   ");
        System.out.println("                                                              ");
        System.out.println("«» The Cloud Network Environment Technology");
        System.out.println("«» Support https://discord.gg/5NUhKuR      [" + NetworkUtils.class.getPackage().getSpecificationVersion() + "]");
        System.out.println("«» Java " + System.getProperty("java.version") + " @" + System.getProperty("user.name") + NetworkUtils.SPACE_STRING + System.getProperty("os.name") + NetworkUtils.SPACE_STRING);
        System.out.println(NetworkUtils.SPACE_STRING);
    }

    public static void headerOut()
    {
        System.out.println("«» The Cloud Network Environment Technology");
        System.out.println("«» Support https://discord.gg/5NUhKuR      [" + NetworkUtils.class.getPackage().getSpecificationVersion() + "]");
        System.out.println("«» Java " + System.getProperty("java.version") + " @" + System.getProperty("user.name") + NetworkUtils.SPACE_STRING + System.getProperty("os.name") + NetworkUtils.SPACE_STRING);
        System.out.println(NetworkUtils.SPACE_STRING);
    }

}