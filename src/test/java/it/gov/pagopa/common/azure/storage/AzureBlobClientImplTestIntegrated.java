package it.gov.pagopa.common.azure.storage;

import com.azure.storage.blob.BlobContainerClient;

import java.io.*;
import java.nio.file.Path;
import java.util.Properties;

/**
 * See confluence page: <a href="https://pagopa.atlassian.net/wiki/spaces/IDPAY/pages/615974424/Secrets+UnitTests">Secrets for UnitTests</a>
 */
@SuppressWarnings({"squid:S3577", "NewClassNamingConvention"}) // suppressing class name not match alert: we are not using the Test suffix in order to let not execute this test by default maven configuration because it depends on properties not pushable. See
class AzureBlobClientImplTestIntegrated extends AzureBlobClientImplTest {

    private final String connectionString;

    public AzureBlobClientImplTestIntegrated() throws IOException {
        try(InputStream storageAccountPropertiesIS = new BufferedInputStream(new FileInputStream("src/test/resources/secrets/storageAccount.properties"))){
            Properties props = new Properties();
            props.load(storageAccountPropertiesIS);
            connectionString=props.getProperty("app.csv.storage.connection-string");
        }
    }

    @Override
    protected AzureBlobClient buildBlobInstance() {
        return new AzureBlobClientImpl(connectionString, "refund");
    }

    @Override
    protected BlobContainerClient mockClientFileOps(File file, String destination, Path downloadPath) {
        // Do Nothing
        return null;
    }

    @Override
    protected BlobContainerClient mockClientStreamOps(InputStream inputStream, String destination) {
        // Do Nothing
        return null;
    }
}
