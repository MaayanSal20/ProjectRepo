package entities;

import java.io.Serializable;

public enum WaitlistStatus implements Serializable {
    WAITING,     // entered waitlist, waiting for table
    OFFERED,     // table offered, waiting for arrival confirmation
    ACCEPTED,    // arrived within 15 minutes
    EXPIRED,     // did not arrive in time
    CANCELLED,   // left waitlist manually
    SEATED_NOW,  // seated immediately (no waiting)
    FAILED       // request failed (validation/db/etc)
}
