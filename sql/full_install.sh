if [ -f mysql_pass.sh ]; then
        . mysql_pass.sh
else
        . mysql_pass.sh.default
fi

mysqldump --ignore-table=${DBNAME}.game_log --ignore-table=${DBNAME}.loginserv_log --ignore-table=${DBNAME}.petitions --add-drop-table -h $DBHOST -u $USER --password=$PASS $DBNAME > l2jdb_full_backup.sql

for tab in \
       install/ally_data.sql \
       install/auction.sql \
       install/auction_bid.sql \
       install/bans.sql \
       install/bonus.sql \
       install/castle.sql \
       install/castle_manor_procure.sql \
       install/castle_manor_production.sql \
       install/character_blocklist.sql \
       install/character_bookmarks.sql \
       install/character_effects_save.sql \
       install/character_friends.sql \
       install/character_hennas.sql \
       install/character_macroses.sql \
       install/character_quests.sql \
       install/character_recipebook.sql \
       install/character_shortcuts.sql \
       install/character_skills.sql \
       install/character_skills_save.sql \
       install/character_subclasses.sql \
       install/character_variables.sql \
       install/characters.sql \
       install/clan_data.sql \
       install/clan_notices.sql \
       install/clan_privs.sql \
       install/clan_skills.sql \
       install/clan_subpledges.sql \
       install/clan_wars.sql \
       install/clanhall.sql \
       install/couples.sql \
       install/craftcount.sql \
       install/cursed_weapons.sql \
       install/dropcount.sql \
       install/epic_boss_spawn.sql \
       install/forts.sql \
       install/forums.sql \
       install/game_log.sql \
       install/games.sql \
       install/global_tasks.sql \
       install/heroes.sql \
       install/item_attributes.sql \
       install/items.sql \
       install/items_delayed.sql \
       install/killcount.sql \
       install/mail.sql \
       install/olympiad_nobles.sql \
       install/petitions.sql \
       install/pets.sql \
       install/posts.sql \
       install/raidboss_points.sql \
       install/raidboss_status.sql \
       install/residence_functions.sql \
       install/server_variables.sql \
       install/seven_signs.sql \
       install/seven_signs_festival.sql \
       install/seven_signs_status.sql \
       install/siege_clans.sql \
       install/siege_doorupgrade.sql \
       install/siege_guards.sql \
       install/siege_territory_members.sql \
       install/topic.sql \
       install/tournament_table.sql \
       install/tournament_teams.sql \
       install/tournament_variables.sql \
       install/vote.sql \
        ; do
                echo Loading $tab ...
                mysql -h $DBHOST -u $USER --password=$PASS -D $DBNAME < $tab
done
./upgrade.sh
