import { FormEvent } from 'react';

import { Link as RouterLink, useNavigate } from 'react-router-dom';

import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Link from '@mui/material/Link';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import useCreateUserMutation from '@/hooks/useCreateUserMutation';
import { Optional } from '@/utils/core-utils';

export default function SignUpForm() {
    const navigate = useNavigate();
    const { createUser } = useCreateUserMutation();

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);

        const username = formData.get('username') as Optional<string>;
        const password = formData.get('password') as Optional<string>;

        if (username && password) {
            await createUser({
                variables: {
                    input: {
                        username,
                        password,
                    }
                }
            });
            navigate('/sign-in');
        }
    };

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100vh',
            }}
        >
            <Avatar sx={{ m: 1, bgcolor: 'secondary.main' }}>
                <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
                {"Sign up"}
            </Typography>
            <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
                <Grid container justifyContent="center">
                    <Grid item xs={10}>
                        <TextField
                            id="username"
                            name="username"
                            label="Username"
                            margin="normal"
                            required
                            fullWidth
                            sx={{ mb: 5 }}
                        />
                    </Grid>
                    <Grid item xs={10}>
                        <TextField
                            id="password"
                            name="password"
                            type="password"
                            label="Password"
                            margin="normal"
                            required
                            fullWidth
                            sx={{ mb: 5 }}
                        />
                    </Grid>
                    <Grid item xs={10}>
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ mt: 3, mb: 2 }}
                        >
                            {"Sign Up"}
                        </Button>
                    </Grid>
                    <Grid item>
                        <Link to="/sign-in" component={RouterLink} variant="body2">
                            {"Already have an account? Sign in"}
                        </Link>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
}
