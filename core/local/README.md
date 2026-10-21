# Local

Room storage for favourite movies. `MovieDao` exposes a paging source for the favourites grid and an observable ID stream for movie hearts. Writes automatically invalidate the Room paging source. Favourite toggling runs in a transaction. Search responses are not cached here.
