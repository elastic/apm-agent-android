package co.elastic.apm.android.sdk.data.network.type;

public interface NetworkType {

    final class None implements NetworkType {

        public static None INSTANCE = new None();

        private None() {
        }

        @Override
        public String getName() {
            return "unavailable";
        }

        @Override
        public String getSubTypeName() {
            return null;
        }
    }

    String getName();

    String getSubTypeName();
}
