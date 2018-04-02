#include "SimpleDexFile.h"

#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>

#include "sha1.h"
#include "Log.h"

#include "adler32.h"

u4 dexComputeChecksum(const DexHeader* pHeader)
{
    const u1* start = (const u1*) pHeader;

    const int nonSum = sizeof(pHeader->magic) + sizeof(pHeader->checksum);

    return (u4) adler32((unsigned char *)start + nonSum, pHeader->fileSize - nonSum);
}

static void dexComputeSHA1Digest(const unsigned char* data, size_t length,
                                 unsigned char digest[])
{
    SHA1_CTX context;
    SHA1Init(&context);
    SHA1Update(&context, data, length);
    SHA1Final(digest, &context);
}

DexFile* dexFileParse(const u1* data, size_t length)
{
    DexFile* pDexFile = NULL;
    const DexHeader* pHeader;
    const u1* magic;
    int result = -1;

    if (length < sizeof(DexHeader)) {
        goto bail;
    }

    pDexFile = (DexFile*) malloc(sizeof(DexFile));
    if (pDexFile == NULL)
        goto bail;      /* alloc failure */
    memset(pDexFile, 0, sizeof(DexFile));

    if (memcmp(data, DEX_OPT_MAGIC, 4) == 0) {
        magic = data;
        if (memcmp(magic+4, DEX_OPT_MAGIC_VERS, 4) != 0) {
            goto bail;
        }
        pDexFile->pOptHeader = ( DexOptHeader*) data;

        data += pDexFile->pOptHeader->dexOffset;
    }

    pHeader = (DexHeader*) data;
    pDexFile->baseAddr = data;
    pDexFile->pHeader = (DexHeader*) data;
    pDexFile->pStringIds = ( DexStringId*) (data + pHeader->stringIdsOff);
    pDexFile->pTypeIds = ( DexTypeId*) (data + pHeader->typeIdsOff);
    pDexFile->pFieldIds = ( DexFieldId*) (data + pHeader->fieldIdsOff);
    pDexFile->pMethodIds = ( DexMethodId*) (data + pHeader->methodIdsOff);
    pDexFile->pProtoIds = ( DexProtoId*) (data + pHeader->protoIdsOff);
    pDexFile->pClassDefs = ( DexClassDef*) (data + pHeader->classDefsOff);
    pDexFile->pLinkData = ( DexLink*) (data + pHeader->linkOff);

    result = 0;

bail:
    if (result != 0 && pDexFile != NULL) {
        dexFileFree(pDexFile);
        pDexFile = NULL;
    }
    return pDexFile;
}


void dexFileFree(DexFile* pDexFile)
{
    if (pDexFile == NULL)
        return;

    free(pDexFile);
}


bool deleteClassDef(u1* oldDexData,size_t oldLen,u1* patchDexData,size_t pathcLen)
{
    if(oldDexData == NULL || patchDexData == NULL || oldLen <= 0 || pathcLen <= 0)
        return false;

    DexFile* oldDexFile = dexFileParse(oldDexData,oldLen);
    DexFile* patchDexFile = dexFileParse(patchDexData,pathcLen);

    if(oldDexFile == NULL || patchDexFile == NULL)
        return false;

    u4 oldClassDefSize = oldDexFile->pHeader->classDefsSize;
    u4 patchClassDefSize = patchDexFile->pHeader->classDefsSize;

    u4 optHeaderOff = oldDexFile->pOptHeader == NULL ? 0 : sizeof(DexOptHeader);

    u4* oldDeleteClassDefIdx = (u4*)malloc(patchClassDefSize*sizeof(u4));
    memset(oldDeleteClassDefIdx, 0, patchClassDefSize* sizeof(u4));

    for(u4 patchIdx = 0;patchIdx < patchClassDefSize;patchIdx++){
        const DexClassDef* patchClassDef =  dexGetClassDef(patchDexFile,patchIdx);
        const char* patchClassDescriptor = dexStringByTypeIdx(patchDexFile,patchClassDef->classIdx);

        for (u4 oldIdx = 0; oldIdx < oldClassDefSize; oldIdx++) {
            const DexClassDef* oldClassDef =  dexGetClassDef(oldDexFile,oldIdx);
            const char* oldClassDescriptor = dexStringByTypeIdx(oldDexFile,oldClassDef->classIdx);

            if (strcmp(patchClassDescriptor,oldClassDescriptor) == 0){
                LOGI("delete class is %s",patchClassDescriptor);
                oldDeleteClassDefIdx[patchIdx] = oldIdx;
            }
        }
    }

    if (oldDeleteClassDefIdx[0] == 0){
        LOGI("can not find the delete class");
        return false;
    }

    //classDefsOff偏移量
    const u4 classDefsOff = optHeaderOff + oldDexFile->pHeader->classDefsOff;

    const u4 sizeClassDef = sizeof(DexClassDef);

    //删除classdef
    for (int i = 0; i < patchClassDefSize; ++i) {

        //从最小的开始覆盖
        u4 minIdex = oldDeleteClassDefIdx[0];
        for (int j = 0; j < patchClassDefSize; ++j) {
            if(oldDeleteClassDefIdx[i] < minIdex){
                minIdex = oldDeleteClassDefIdx[i];
                oldDeleteClassDefIdx[i] = -1;
                break;
            }
        }

        //往前覆盖
        const u4 deleteClassDefOff = classDefsOff + minIdex * sizeClassDef;
        const u4 afterDeleteClassDefOff = classDefsOff + (minIdex+1) * sizeClassDef;
        const u4 coverCount = (oldClassDefSize - minIdex - 1) * sizeClassDef;

        memcpy(oldDexData+deleteClassDefOff,oldDexData+afterDeleteClassDefOff,coverCount);
    }

    //删除最后多于的classDef
    const u4 lastClassDefoff = classDefsOff + (oldClassDefSize - patchClassDefSize)* sizeClassDef;
    memset(oldDexData+lastClassDefoff,0, sizeClassDef * patchClassDefSize);

    //修改ClassDefSize
    oldDexFile->pHeader->classDefsSize = oldDexFile->pHeader->classDefsSize - patchClassDefSize;

    //修改maplist
    DexMapList* dexMapList = dexGetMap(oldDexFile);
    if (dexMapList != NULL){
        u4 mapSize = dexMapList->size;
        u1* mapListData = oldDexData + oldDexFile->pHeader->mapOff + sizeof(u4);
        DexMapItem* dexMapItem = (DexMapItem*)mapListData;
        for (int i = 0; i < mapSize ; ++i) {
            if (dexMapItem->type == kDexTypeClassDefItem){
                dexMapItem->size = oldDexFile->pHeader->classDefsSize;
                break;
            }
            dexMapItem++;
        }
    }

    //修改SHA1
    const int nonSum = sizeof(oldDexFile->pHeader->magic) + sizeof(oldDexFile->pHeader->checksum) + kSHA1DigestLen;
    unsigned char sha1Digest[kSHA1DigestLen];
    dexComputeSHA1Digest(oldDexFile->baseAddr+nonSum,oldLen - optHeaderOff - nonSum,sha1Digest);
    memcpy(oldDexFile->pHeader->signature, sha1Digest, kSHA1DigestLen);

    //修改adler32
    u4 adler32 = dexComputeChecksum(oldDexFile->pHeader);
    oldDexFile->pHeader->checksum = adler32;

    return true;

}
