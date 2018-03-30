#include "SimpleDexFile.h"

#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>

#include "Log.h"

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
    memset(oldDeleteClassDefIdx, 0, sizeof(patchClassDefSize* sizeof(u4)));

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

    //classDefsOff偏移量
    const u4 classDefsOff = optHeaderOff + oldDexFile->pHeader->classDefsOff;

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
        const u4 deleteClassDefOff = classDefsOff + minIdex * sizeof(DexClassDef);
        const u4 afterDeleteClassDefOff = classDefsOff + (minIdex+1) * sizeof(DexClassDef);
        const u4 coverCount = (oldClassDefSize - minIdex - 1) * sizeof(DexClassDef);

        memcpy(oldDexData+deleteClassDefOff,oldDexData+afterDeleteClassDefOff,coverCount);
    }

    //删除最后多于的classDef
    const u4 lastClassDefoff = classDefsOff + (oldClassDefSize - patchClassDefSize)* sizeof(DexClassDef);
    memset(oldDexData+lastClassDefoff,0, sizeof(DexClassDef) * patchClassDefSize);

    //修改ClassDefSize
    oldDexFile->pHeader->classDefsSize = oldDexFile->pHeader->classDefsSize - patchClassDefSize;

    return true;

}
