//
// Created by AwangDani on 17/10/2024.
//
#include <string>
#include "jni.h"
#include "obfuscate.h"


extern "C"
JNIEXPORT jstring JNICALL
Java_com_dxablack_AttackFunction_mainCommand(JNIEnv *env, jclass clazz) {
    const char* command = OBFUSCATE("mdk4");
    return env->NewStringUTF(command);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_dxablack_AttackFunction_rootFsPath(JNIEnv *env, jclass clazz) {
    const char* path = OBFUSCATE("/data/local/nhsystem/kalifs");
    return env->NewStringUTF(path);
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_dxablack_AttackFunction_attackModes(JNIEnv *env, jclass clazz) {
    const char *modes[] = {
            OBFUSCATE("111;b;Beacon Flooding"),
            OBFUSCATE("222;a;Authentication Denial-Of-Service"),
            OBFUSCATE("333;p;SSID Probing and Bruteforcing"),
            OBFUSCATE("444;d;Deauthentication and Disassociation"),
            OBFUSCATE("555;m;Michael Countermeasures Exploitation"),
            OBFUSCATE("666;e;EAPOL Start and Logoff Packet Injection"),
            OBFUSCATE("777;s;Attacks for IEEE 802.11s mesh networks"),
            OBFUSCATE("888;w;WIDS Confusion"),
            OBFUSCATE("999;f;Packet Fuzzer"),
            OBFUSCATE("1010;x;WiFi Protocol Vulnerability Testing")
    };

    jobjectArray result;
    int numModes = 10;

    // Membuat array Java untuk menyimpan hasil string mode
    result = env->NewObjectArray(numModes, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    // Memasukkan mode serangan ke array Java
    for (int i = 0; i < numModes; i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(modes[i]));
    }

    return result;
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_dxablack_AttackFunction_attackParameter(JNIEnv *env, jclass clazz) {
    const char *params[] = {
            // Parameters for Beacon Flooding (Mode b)
            OBFUSCATE("111;edittext;-n;Use SSID;Instead of randomly generated ones;\"Hacked By Dx4\""),
            OBFUSCATE("111;checkbox;-a;Non-printable characters;Use non-printable characters in SSIDs;false"),
            OBFUSCATE("111;edittext;-f;List SSID File;Read SSIDs from file;/sdcard/SSID.txt"),
            OBFUSCATE("111;edittext;-v;MAC and SSID File;Read MACs and SSIDs from file;/sdcard/MAC_SSID.txt"),
            OBFUSCATE("111;edittext;-t;Network Type;0 = Managed, 1 = Ad-Hoc;0"),
            OBFUSCATE("111;edittext;-w;Encryption Types;Valid options: n = None, w = WEP, t = TKIP, a = AES;wta"),
            OBFUSCATE("111;edittext;-b;Bitrate;Select bitrate for networks (11 Mbit for b, 54 Mbit for g);g"),
            OBFUSCATE("111;checkbox;-m;Valid MAC Address;Use valid access point MAC from OUI database;false"),
            OBFUSCATE("111;checkbox;-h;Channel Hopping;Hop to channel where network is spoofed;false"),
            OBFUSCATE("111;edittext;-c;Channel;Create fake networks on specified channel;6"),
            OBFUSCATE("111;edittext;-s;Packets Per Second;Set speed in packets per second;50"),
            OBFUSCATE("111;edittext;-i;User-defined IE;Add custom IE(s) in hexadecimal;"),

            // Parameters for Authentication Denial-Of-Service (Mode a)
            OBFUSCATE("222;edittext;-a;AP Mac;Only test the specified AP;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("222;checkbox;-m;Valid Client MAC;Use valid client MAC from OUI database;false"),
            OBFUSCATE("222;edittext;-i;Intelligent Test on AP;Perform intelligent test on AP;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("222;edittext;-s;Packets Per Second;Custom packets per-second;2500"),

            // Parameters for SSID Probing and Bruteforcing (Mode p)
            OBFUSCATE("333;edittext;-e;SSID to Probe;Specify SSID to probe for;NetworkName"),
            OBFUSCATE("333;edittext;-f;SSID List File;Read SSIDs from file for bruteforcing hidden SSIDs;/sdcard/SSID_list.txt"),
            OBFUSCATE("333;edittext;-t;AP MAC Address;Set MAC address of target AP;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("333;edittext;-s;Packets Per Second;Set speed for probing (Default: 400);400"),
            OBFUSCATE("333;edittext;-b;Character Sets;Use full Bruteforce mode with character sets (n = Numbers, u = Uppercase, l = Lowercase, s = Symbols);nul"),
            OBFUSCATE("333;edittext;-p;Continue from Word;Continue bruteforcing starting at word;word123"),
            OBFUSCATE("333;edittext;-r;Channel;Probe request tests (mod-musket);6"),

            // Parameters for Deauthentication and Disassociation (Mode d)
            OBFUSCATE("444;edittext;-w;Whitelist File;Read file containing MACs not to attack;/sdcard/whitelist.txt"),
            OBFUSCATE("444;edittext;-b;Blacklist File;Read file containing MACs to attack;/sdcard/blacklist.txt"),
            OBFUSCATE("444;edittext;-s;Packets Per Second;Set speed in packets per second (Default: unlimited);"),
            OBFUSCATE("444;checkbox;-x;Enable Full IDS Stealth;Match all Sequence Numbers for stealth mode;false"),
            OBFUSCATE("444;edittext;-c;Channel Hopping;Enable channel hopping and set speed (milliseconds);1000"),
            OBFUSCATE("444;edittext;-E;AP ESSID;Specify an AP ESSID to attack;NetworkName"),
            OBFUSCATE("444;edittext;-B;AP BSSID;Specify an AP BSSID to attack;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("444;edittext;-S;Station MAC;Specify a station MAC address to attack;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("444;edittext;-W;Whitelist Station MAC;Specify a whitelist station MAC address;FF:FF:FF:FF:FF:FF"),

            // Parameters for Michael Countermeasures Exploitation (Mode m)
            OBFUSCATE("555;edittext;-t;Target AP BSSID;Set target AP that runs TKIP encryption;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("555;checkbox;-j;QoS Exploit;Use the QoS exploit for fewer packets;false"),
            OBFUSCATE("555;edittext;-s;Packets Per Second;Set speed in packets per second (Default: 400);400"),
            OBFUSCATE("555;edittext;-w;Wait Time;Wait time between packet bursts (seconds);10"),
            OBFUSCATE("555;edittext;-n;Random Packets Per Burst;Number of random packets per burst;70"),

            // Parameters for EAPOL Start and Logoff Packet Injection (Mode e)
            OBFUSCATE("666;edittext;-t;Target BSSID;Set target WPA AP BSSID;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("666;edittext;-s;Packets Per Second;Set speed in packets per second (Default: 400);400"),
            OBFUSCATE("666;checkbox;-l;Use Logoff Messages;Inject EAPOL Logoff messages to kick clients;false"),

            // Parameters for IEEE 802.11s mesh networks attacks (Mode s)
            OBFUSCATE("777;edittext;-f;Fuzzing Type;Select fuzzing type (1-5);1"),
            OBFUSCATE("777;edittext;-b;Impersonated Meshpoint MAC;Use MAC address of impersonated meshpoint;"),
            OBFUSCATE("777;edittext;-p;Path Request Flood MAC;Use MAC address for path request flood;"),
            OBFUSCATE("777;checkbox;-l;Create Loops;Modify Path Replies to create loops;false"),
            OBFUSCATE("777;edittext;-s;Packets Per Second;Set speed in packets per second (Default: 100);100"),
            OBFUSCATE("777;edittext;-n;Mesh Network ID;Specify mesh network ID;"),

            // Parameters for WIDS Confusion (Mode w)
            OBFUSCATE("888;edittext;-e;WDS Network SSID;Specify target WDS network SSID;"),
            OBFUSCATE("888;edittext;-c;Channel Hopping;Set channel hopping speed (milliseconds);3000"),
            OBFUSCATE("888;checkbox;-z;Activate WIDS Exploit;Activate Zero_Chaos' WIDS exploit;false"),
            OBFUSCATE("888;edittext;-s;Packets Per Second;Set speed in packets per second (Default: 100);100"),

            // Parameters for Packet Fuzzer (Mode f)
            OBFUSCATE("999;edittext;-s;Packet Sources;Specify packet sources (a, b, c, p);a"),
            OBFUSCATE("999;edittext;-m;Packet Modifiers;Specify modifiers (n, b, m, s, t, c, d);n"),
            OBFUSCATE("999;edittext;-c;Channel Hopping Speed;Set speed for channel hopping (milliseconds);3000"),
            OBFUSCATE("999;edittext;-p;Packets Per Second;Set speed in packets per second (Default: 250);250"),

            // Parameters for WiFi Protocol Vulnerability Testing (Mode x)
            OBFUSCATE("1010;edittext;-s;Packets Per Second;Set speed in packets per second (Default: unlimited);unlimited"),
            OBFUSCATE("1010;edittext;-c;Channel Hopping;Enable channel hopping and set speed (milliseconds);3000"),
            OBFUSCATE("1010;edittext;-v;Vendor;Specify vendor for PoC tests (default: all);all"),
            OBFUSCATE("1010;edittext;-B;AP BSSID;Set target AP BSSID;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("1010;edittext;-S;Source MAC;Set source MAC address;FF:FF:FF:FF:FF:FF"),
            OBFUSCATE("1010;edittext;-T;Target MAC;Set target MAC address;FF:FF:FF:FF:FF:FF")
            // Tambahkan parameter lainnya sesuai yang ada di Java code
    };

    int numParams = sizeof(params) / sizeof(params[0]);

    // Membuat array Java untuk menyimpan hasil string parameter
    jobjectArray result = env->NewObjectArray(numParams, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    // Memasukkan parameter ke array Java
    for (int i = 0; i < numParams; i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(params[i]));
    }

    return result;
}
