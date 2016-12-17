package michael.findata.algoquant.execution.listener;

import com.numericalmethod.algoquant.execution.datatype.depth.Depth;

public interface DepthListener {
	void depthUpdated (Depth depth);
}
