package io.livekit.server;

public class TestServerSDK {
    public void test() {
        EgressServiceClient client = EgressServiceClient.createClient("", "", "");
        EncodedOutputs outputs = new EncodedOutputs(
            new LiveKitEgress.EncodedFileOutput(),
            null, null, null);
    }
}
