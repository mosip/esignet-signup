package io.mosip.signup;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = {"test"})
public class SignUpServiceApplicationTest {
    @Test
    public void test() {
        SignUpServiceApplication.main(new String[] {});
        Assert.assertNotNull(SignUpServiceApplication.class);
    }
}
