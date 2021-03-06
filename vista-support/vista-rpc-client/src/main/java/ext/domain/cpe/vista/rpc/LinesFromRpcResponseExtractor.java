package org.osehra.cpe.vista.rpc;

import org.springframework.dao.DataAccessException;

/**
 * Implementation of the RpcResponseExtractor interface that returns the {@link RpcResponse} split into a <code>String[]</code>
 * one String per line.
 */
public class LinesFromRpcResponseExtractor implements RpcResponseExtractor<String[]> {

    @Override
    public String[] extractData(RpcResponse response) throws RpcResponseExtractionException {
        return response.toLines();
    }
}
