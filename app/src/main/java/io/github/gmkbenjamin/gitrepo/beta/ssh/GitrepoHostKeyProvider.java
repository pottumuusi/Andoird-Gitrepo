package io.github.gmkbenjamin.gitrepo.beta.ssh;

import android.content.Context;
import android.util.Log;

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.common.util.SecurityUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;

public class GitrepoHostKeyProvider extends AbstractKeyPairProvider {
    private final static String TAG = GitrepoHostKeyProvider.class.getSimpleName();
    private final static String HOST_KEY_FILENAME = "hostKey.cert";
    private final static String ALGORITHM = "RSA";

    private Context context;
    private KeyPair keyPair;

    public GitrepoHostKeyProvider(Context context) {
        this.context = context;
    }

    @Override
    public Iterable<KeyPair> loadKeys() {
        if (keyPair == null) {
            FileInputStream hostKeyInputStream = null;

            try {
                hostKeyInputStream = context.openFileInput(HOST_KEY_FILENAME);

                try {
                    ObjectInputStream ois = new ObjectInputStream(hostKeyInputStream);
                    keyPair = (KeyPair) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    Log.e(TAG, "Cannot read host key file.", e);
                }
            } catch (FileNotFoundException e) {
                Log.i(TAG, "No host key available.");
            } finally {
                if (hostKeyInputStream != null) {
                    try {
                        hostKeyInputStream.close();
                    } catch (IOException e) {
                        Log.w(TAG, "I/O error while closing host key file.", e);
                    }
                }
            }

            if (keyPair == null) {
                keyPair = generateKeyPair();

                if (keyPair == null) {
//					return new KeyPair[0];
                    return new ArrayList<KeyPair>();
                }

                FileOutputStream hostKeyOutputStream = null;
                try {
                    hostKeyOutputStream = context.openFileOutput(HOST_KEY_FILENAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Cannot create host key file.", e);
                }

                if (hostKeyOutputStream != null) {
                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(hostKeyOutputStream);
                        oos.writeObject(keyPair);
                        oos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error while writing host key to file.", e);
                    }
                }
            }
        }

        List<KeyPair> kp = new ArrayList<KeyPair>();
        kp.add(keyPair);
        return kp;
//		return new KeyPair[] { keyPair };
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = SecurityUtils.getKeyPairGenerator(ALGORITHM);
            Log.i(TAG, "Generating host key...");
            KeyPair kp = generator.generateKeyPair();
            return kp;
        } catch (Exception e) {
            Log.e(TAG, "Unable to generate keypair.", e);
            return null;
        }
    }

}
