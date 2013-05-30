package blocksize;

public class BlockSizeRunAll {

	public static void main(String[] args) {
		BlockSizeTableNaive.main(new String[]{});
		BlockSizeTableNaive2.main(new String[]{});
		BlockSizeTableContract.main(new String[]{});
		BlockSizeTableGradient.main(new String[]{});
	}
}
