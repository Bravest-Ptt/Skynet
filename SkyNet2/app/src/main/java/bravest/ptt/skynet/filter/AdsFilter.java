package bravest.ptt.skynet.filter;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import bravest.ptt.skynet.R;
import bravest.ptt.skynet.app.App;
import bravest.ptt.skynet.core.ProxyConfig;
import bravest.ptt.skynet.core.filter.DomainFilter;
import bravest.ptt.skynet.core.tcpip.CommonMethods;

/**
 * Created by pengtian on 2017/7/25.
 */

public class AdsFilter implements DomainFilter {

    private Map<String, Integer> mDomainMap = new HashMap<>();
    private SparseIntArray mIpMask = new SparseIntArray();

    @Override
    public void prepare() {

        if (mDomainMap.size() != 0 || mIpMask.size() != 0) {
            return;
        }

        InputStream in = getHostInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")
                        || !TextUtils.isDigitsOnly(String.valueOf(line.charAt(0)))) {
                    continue;
                }

                String[] parts = line.split(" ");
                if (parts.length == 2
                        && !"localhost".equalsIgnoreCase(parts[1])) {
                    String ipStr = parts[0];
                    int ip = CommonMethods.ipStringToInt(ipStr);
                    mDomainMap.put(parts[1], ip);
                    mIpMask.put(ip, 1);
                }
            }
        } catch (IOException ex) {
                ex.printStackTrace(System.err);
        } finally {
            try {
                reader.close();
                in.close();
            } catch (IOException ex) {
                    ex.printStackTrace(System.err);
            }
        }
    }

    @Override
    public boolean needFilter(String domain, int ip) {

        if (domain == null) {
            return false;
        }

        boolean filter = false;
        if (mIpMask.get(ip, -1) == 1) {
            filter = true;
        }
        if (Pattern.matches("\\d+\\.\\d+\\.\\d+\\.\\d+", domain.trim())) {
            int newIp = CommonMethods.ipStringToInt(domain.trim());
            filter = filter || (mIpMask.get(newIp, -1) == 1);
        }
        String key = domain.trim();
        if (mDomainMap.containsKey(key)) {
            filter = true;
            int oldIP = mDomainMap.get(key);
            if (!ProxyConfig.isFakeIP(ip) && ip != oldIP) {
                mDomainMap.put(key, ip);
                mIpMask.put(ip, 1);
            }
        }

        return filter;
    }

    private InputStream getHostInputStream() {
        InputStream in = null;
        Context context = App.getInstance();
        File file = new File(context.getExternalCacheDir(), "host.txt");
        if (file.exists()) {
            try {
                in = new FileInputStream(file);
            } catch (IOException ex) {
                    ex.printStackTrace(System.err);
            }
        }
        if (in == null) {
            in = context.getResources().openRawResource(R.raw.hosts);
        }
        return in;
    }
}
