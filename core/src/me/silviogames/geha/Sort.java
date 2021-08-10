package me.silviogames.geha;

import com.badlogic.gdx.utils.IntArray;

public class Sort
{

	private static void quickSort( IntArray indices, IntArray values, int low, int high, boolean descending )
	{
		if ( values == null || values.size < 2 )
		{
			return;
		}

		if ( low >= high )
		{
			return;
		}

		// pick the pivot
		int middle = low + ( high - low ) / 2;
		int pivot = values.get( middle );

		// make left < pivot and right > pivot
		int i = low, j = high;
		while ( i <= j )
		{
			if ( !descending )
			{
				while ( values.get( i ) < pivot )
				{
					i++;
				}

				while ( values.get( j ) > pivot )
				{
					j--;
				}
			} else
			{
				while ( values.get( i ) > pivot )
				{
					i++;
				}

				while ( values.get( j ) < pivot )
				{
					j--;
				}
			}

			if ( i <= j )
			{
				indices.swap( i, j );
				values.swap( i, j );
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if ( low < j )
		{
			quickSort( indices, values, low, j, descending );
		}

		if ( high > i )
		{
			quickSort( indices, values, i, high, descending );
		}
	}

	public static void quick_sort( IntArray indices, IntArray values, boolean descending )
	{
		quickSort( indices, values, 0, indices.size - 1, descending );
	}

}
