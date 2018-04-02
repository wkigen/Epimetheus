#include <string>
#include <malloc.h>

#define BASE 65521U

extern __inline__ unsigned long adler32(unsigned char *buf, int len)
{
    unsigned long adler=1;
    unsigned long s1 = adler & 0xffff;
    unsigned long s2 = (adler >> 16) & 0xffff;

    int i;
    for (i = 0; i < len; i++)
    {
        s1 = (s1 + buf[i]) % BASE;
        s2 = (s2 + s1) % BASE;
    }
    return (s2 << 16) + s1;
}
