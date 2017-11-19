

TALLINN_LAT <-  59.436962;
TALLINN_LON <-  24.753574;
distance <- function(lat, lon) {
  return( sqrt( (lat-TALLINN_LAT)^2 + (lon-TALLINN_LON)^2 ) )
}
p <- function(v) {
  Reduce(f=paste, x = v %>% unique %>% sort)
}
stop <- read.csv("stops.txt") %>% filter(distance(stop_lat, stop_lon) < 0.1) %>% select(stop_id, stop_lat, stop_lon, stop_name) 
times <- read.csv("stop_times.txt") %>% filter(stop_id %in% stop$stop_id) 
joined <- stop %>% inner_join(times, by = "stop_id") 

trips <- read.csv("trips.txt") %>% filter(trip_id %in% times$trip_id)
routes <- read.csv("routes.txt") 
trips_joined <- trips %>% inner_join(routes, by = "route_id") %>% filter(route_type == 0)

joined2 <- joined %>% inner_join(trips_joined, by = "trip_id") %>% select(-c(route_id, trip_id, service_id, shape_id, wheelchair_accessible, direction_code))
summarised <- joined2 %>% group_by(stop_id, route_short_name) %>% mutate(schedule = p(arrival_time)) %>% summarise(name = first(stop_name), schedule = first(schedule), lat=first(stop_lat), lon = first(stop_lon)) 
#summarised %>% group_by(stop_id, route_short_name) %>% summarise(lat = first(lat), lon = first(lon), name=first(name))
write.csv(summarised[,c("stop_id", "route_short_name", "name", "lat", "lon", "schedule")], "tram_stops.csv")
# trips <- read.csv("trips.txt") %>% group_by(trip_long_name) %>% summarise(id = head(trip_id, 1)) %>%
#  arrange(id)

# G R A P H
times_tram <- times %>% filter(stop_id %in% summarised$stop_id)
times_all <- times_tram %>% inner_join(trips, by = "trip_id") %>% inner_join(routes, by = "route_id")
trip_unique <- times_all %>% 
  group_by(route_id, trip_id) %>% 
  summarise(n = n()) %>% 
  group_by(route_id) %>% 
  arrange(n) %>%
  filter(n == max(n)) %>%
  summarise(trip_id = first(trip_id)) 
get_edges <- function (trip_ids) {
  times_joined <- times_all %>% filter(trip_id %in% trip_ids)
  starts = c()
  finishes = c()
  minutes = c()
  route_short_name = c()
  route_color = c()
  for (i in 1:(nrow(times_joined) - 1)) {
    if (times_joined[i,]$trip_id == times_joined[i+1,]$trip_id) {
      starts[i] <- times_joined[i,]$stop_id
      finishes[i] <- times_joined[i + 1,]$stop_id
      time1 <- times_joined[i,]$departure_time %>% as.POSIXct(format="%H:%M:%S")
      time2 <- times_joined[i + 1,]$departure_time %>% as.POSIXct(format="%H:%M:%S")
      minutes[i] <- as.numeric(time2 - time1)
      route_short_name[i] <- times_joined[i,]$route_short_name %>% as.character
      route_color[i] <- times_joined[i,]$route_color %>% as.character
    }
  }
  edges <- data.frame(starts, finishes, minutes, route_short_name)
  return(edges %>% na.omit)
}
get_edges(trip_unique$trip_id) %>% rbind(get_edges(trip_unique$trip_id + 1)) 
write.csv(get_edges(trip_unique$trip_id) %>% rbind(get_edges(trip_unique$trip_id + 1)) , "edges.csv")



