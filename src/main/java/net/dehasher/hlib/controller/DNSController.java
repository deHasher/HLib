package net.dehasher.hlib.controller;

import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.Encrypt;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

public class DNSController {
    private static final SimpleResolver RESOLVER;

    static {
        try {
            SimpleResolver resolver = new SimpleResolver(Encrypt.IP_DNS.value);
            resolver.setTimeout(Duration.ofSeconds(5));
            RESOLVER = resolver;
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to initialize DNS resolver", e);
        }
    }

    public static List<String> parseTXTRecords(String domain) {
        try {
            Lookup.getDefaultCache(DClass.IN).clearCache();
            Lookup lookup = new Lookup(domain, Type.TXT);
            if (RESOLVER != null) lookup.setResolver(RESOLVER);
            lookup.setCache(null);

            Record[] records = lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                lookup = new Lookup(domain, Type.TXT);
                lookup.setCache(null);
                records = lookup.run();
                if (lookup.getResult() != Lookup.SUCCESSFUL) {
                    Informer.send("NSLookup error: " + lookup.getErrorString());
                    return List.of();
                }
            }

            return Stream.of(records)
                    .filter(record -> record instanceof TXTRecord)
                    .map(record -> (TXTRecord) record)
                    .map(record -> Tools.join("", record.getStrings()))
                    .toList();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return List.of();
    }
}